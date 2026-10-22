package com.groupware.controller;

import com.groupware.dao.BoardDAO;
import com.groupware.dto.*;
import com.groupware.service.FileStorageService;
import jakarta.servlet.http.*;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@Controller
public class BoardWriteController {
    private final BoardDAO boardDAO;
    private final FileStorageService storage;
    public BoardWriteController(BoardDAO boardDAO,FileStorageService storage) { this.boardDAO=boardDAO;this.storage=storage; }
    private EmployeeDTO actor(HttpSession session) { return (EmployeeDTO)session.getAttribute("loginEmp"); }
    private BoardDTO editable(int boardNo,HttpSession session) {
        BoardDTO board=boardDAO.getBoardDetail(boardNo);
        if(board==null) throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        if(board.getEmpNo()!=actor(session).getEmpNo() && !"Y".equals(actor(session).getManager())) throw new IllegalArgumentException("수정 권한이 없습니다.");
        return board;
    }
    private void validate(String title,String content) {
        if(title==null || title.isBlank() || title.length()>200 || content==null || content.isBlank()) throw new IllegalArgumentException("제목과 내용을 확인해주세요.");
    }
    private List<BoardFileDTO> store(MultipartFile[] files) throws IOException {
        List<BoardFileDTO> result=new ArrayList<>();
        try {
            if(files!=null) for(MultipartFile file:files) if(!file.isEmpty()) result.add(storage.store(file));
            return result;
        } catch(Exception exception) { cleanFiles(result); throw exception; }
    }
    private void cleanFiles(List<BoardFileDTO> files) throws IOException {
        for(BoardFileDTO file:files) storage.delete(file.getSavedName());
    }

    @GetMapping("/boardWrite.do")
    public String form(@RequestParam(defaultValue="1") int type,HttpServletRequest request) {
        if(type<1 || type>3 || (type==1 && !"Y".equals(actor(request.getSession()).getManager()))) throw new IllegalArgumentException("작성 권한이 없습니다.");
        request.setAttribute("type",type);return "boardWrite";
    }
    @PostMapping("/boardWrite.do")
    public String write(@RequestParam int type,@RequestParam String title,@RequestParam String content,
                        @RequestParam(required=false) MultipartFile[] files,HttpServletRequest request) throws IOException {
        form(type,request);validate(title,content);
        BoardDTO board=new BoardDTO();board.setBoardType(type);board.setTitle(title);board.setContent(content);board.setEmpNo(actor(request.getSession()).getEmpNo());
        List<BoardFileDTO> uploaded=store(files);
        if(!boardDAO.insertBoard(board,uploaded)) { cleanFiles(uploaded);throw new IllegalArgumentException("게시글 저장에 실패했습니다."); }
        return "redirect:boardList.do?type="+type;
    }
    @GetMapping("/boardUpdate.do")
    public String edit(@RequestParam int boardNo,HttpServletRequest request) {
        BoardDTO board=editable(boardNo,request.getSession());
        request.setAttribute("board",board);request.setAttribute("type",board.getBoardType());
        request.setAttribute("fileList",boardDAO.getFilesByBoardNo(boardNo));return "boardWrite";
    }
    @PostMapping("/boardUpdate.do")
    public String update(@RequestParam int boardNo,@RequestParam String title,@RequestParam String content,
                         @RequestParam(required=false) MultipartFile[] files,@RequestParam(required=false) List<Integer> delFiles,HttpSession session) throws IOException {
        editable(boardNo,session);validate(title,content);
        List<BoardFileDTO> existing=boardDAO.getFilesByBoardNo(boardNo);
        Set<Integer> allowed=new HashSet<>();existing.forEach(file->allowed.add(file.getFileNo()));
        List<Integer> deleting=delFiles==null?List.of():delFiles;
        if(!allowed.containsAll(deleting)) throw new IllegalArgumentException("다른 게시글의 첨부파일입니다.");
        BoardDTO board=new BoardDTO();board.setBoardNo(boardNo);board.setTitle(title);board.setContent(content);
        List<BoardFileDTO> uploaded=store(files);
        if(!boardDAO.updateBoard(board,uploaded,deleting)) { cleanFiles(uploaded);throw new IllegalArgumentException("게시글 수정에 실패했습니다."); }
        for(BoardFileDTO file:existing) if(deleting.contains(file.getFileNo())) storage.delete(file.getSavedName());
        return "redirect:boardDetail.do?boardNo="+boardNo;
    }
    @RequestMapping(value="/boardDelete.do",method={RequestMethod.GET,RequestMethod.POST})
    public String delete(@RequestParam int boardNo,HttpSession session) throws IOException {
        BoardDTO board=editable(boardNo,session);List<BoardFileDTO> files=boardDAO.getFilesByBoardNo(boardNo);
        if(!boardDAO.deleteBoard(boardNo)) throw new IllegalArgumentException("게시글 삭제에 실패했습니다.");
        cleanFiles(files);return "redirect:boardList.do?type="+board.getBoardType();
    }
    @GetMapping("/fileDownload.do")
    public void download(@RequestParam int fileNo,HttpServletResponse response) throws IOException {
        BoardFileDTO file=boardDAO.getFileDetail(fileNo);
        if(file==null || !Files.isRegularFile(storage.path(file.getSavedName()))) { response.sendError(404);return; }
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",ContentDisposition.attachment().filename(file.getOrgName(),StandardCharsets.UTF_8).build().toString());
        response.setContentLengthLong(Files.size(storage.path(file.getSavedName())));
        Files.copy(storage.path(file.getSavedName()),response.getOutputStream());
    }
}
