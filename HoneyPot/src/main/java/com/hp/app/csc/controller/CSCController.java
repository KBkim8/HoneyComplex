package com.hp.app.csc.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hp.app.csc.service.CSCService;
import com.hp.app.csc.vo.FAQCategoryVo;
import com.hp.app.csc.vo.FAQVo;
import com.hp.app.csc.vo.QNACategoryVo;
import com.hp.app.csc.vo.QNAVo;
import com.hp.app.csc.vo.ReportCategoryVo;
import com.hp.app.csc.vo.ReportVo;
import com.hp.app.csc.vo.SearchVo;
import com.hp.app.member.vo.MemberVo;
import com.hp.app.page.vo.PageVo;

import lombok.RequiredArgsConstructor;
import oracle.jdbc.proxy.annotation.Post;

@Controller
@RequiredArgsConstructor
public class CSCController {

	private final CSCService service;
	
	// 회원
	
	// FAQ 조회(화면)
	@GetMapping("csc/faq")
	public String getFAQList(Model model, String page, String searchType, String searchValue) {
		
		if(page == null) {
			page = "1";
		}
		
		//검색값 저장
		Map<String, String> searchVo = new HashMap<>();
		searchVo.put("searchType", searchType);
		searchVo.put("searchValue", searchValue);
		
		int listCount = service.getFAQCnt(searchVo);
		int currentPage = Integer.parseInt(page);
		int pageLimit = 5;
		int boardLimit = 8;
		
		PageVo pvo = new PageVo(listCount, currentPage, pageLimit, boardLimit);
		

		
		List<FAQVo> fList = service.getFAQList(pvo,searchVo);
		List<FAQCategoryVo> cList = service.getFAQCatList();
		
		model.addAttribute("fList", fList);
		model.addAttribute("cList", cList);
		model.addAttribute("pvo", pvo);
		model.addAttribute("searchVo", searchVo);
		
		return "csc/member/faq";
	}
	
	// FAQ 상세조회
	@GetMapping("csc/faq/detail")
	@ResponseBody
	public FAQVo getFAQDetail(String fno) throws Exception {
		FAQVo vo = service.getFAQByNo(fno);
		
		if(vo == null) {
			throw new Exception("상세조회 에러");
		}
		
		return vo;
	}
	
	// 문의하기(화면)
	@GetMapping("csc/inquiry")
	public String inquiry(Model model) throws Exception {
		
		List<QNACategoryVo> cList = service.getQNACatList();
		
		if(cList == null) {
			throw new Exception("문의등록 카테고리 조회 에러");
		}
		
		model.addAttribute("cList", cList);
		
		return "csc/member/inquiry";
	}
	
	// 문의등룍
	@PostMapping("csc/inquiry")
	public String inquiry(QNAVo vo) throws Exception {
		
		if(vo.getTitle() == null || vo.getContent() == null) {
			throw new Exception("게시판 작성 에러");
		}
		
		vo.setMemberNo("1");
		int result = service.insertInquiry(vo);
		
		if(result != 1) {
			return "redirect:/errorPage";
		}
		
		return "redirect:/csc/inquiry-list";
	}

	
	// 문의목록 (화면)
	@GetMapping("csc/inquiry-list")
	public String inquiryList(Model model) {
		String no = "1";
		List<QNAVo> qList = service.getQNAList(no);
		
		model.addAttribute("qList", qList);
		
		return "csc/member/inquiry-list";
	}
	
	// 문의상세조회 (화면)
	@GetMapping("csc/qna/detail")
	@ResponseBody
	public QNAVo getQNADetail(String qno) throws Exception {
		
		String no = "1";
		
		if("".equals(qno) || qno == null) {
			throw new Exception("QNA 상세조회 실패");
		}
		
		QNAVo qvo = new QNAVo();
		qvo.setMemberNo(no);
		qvo.setNo(qno);
		
		QNAVo vo = service.getQNAByNo(qvo);
		
		if(vo == null) {
			throw new Exception("QNA 상세조회 실패");
		}
		
		return vo;
	}
	
	// 문의 내역 삭제
	@GetMapping("/csc/inquiry/delete")
	public String deleteInquiry(String bno) throws Exception {
		
		QNAVo vo = new QNAVo();
		vo.setNo(bno);
		vo.setMemberNo("1");
		
		int result = service.deleteInquiry(vo);
		
		if(result != 1) {
			throw new Exception("문의내역 에러...");
		}
		
		return "redirect:/csc/inquiry-list";
	}
	
	
	// 신고하기(화면)
	@GetMapping("csc/report")
	public String report(Model model) {
		
		List<ReportCategoryVo> cList = service.getReportCatList();
		
		model.addAttribute("cList", cList);
		
		return "csc/member/report";
	}
	
	// 신고하기
	@PostMapping("csc/report")
	public String report(ReportVo vo) throws Exception {
		
		vo.setReporter("1");
		System.out.println(vo);
		
		int result = service.insertReport(vo);
		
		if(result != 1) {
			throw new Exception("신고하기 에러");
		}
		
		return "redirect:/csc/report-list";
		
	}
	
	// 신고 대상 회원 List 조회
	@PostMapping("csc/report/memberList")
	@ResponseBody
	public List<MemberVo> getMemberList(SearchVo vo){		
	
		List<MemberVo> mList = service.getMemberList(vo);
		
		return mList;
		
	}
	
	// 신고 대상 회원 조회
	@PostMapping("csc/report/member")
	@ResponseBody
	public MemberVo getMemberByNo(String mno) {
		
		System.out.println(mno);
		
		MemberVo vo = service.getMemberByNo(mno);
		
		return vo;
		
	}
	
	// 신고목록 (화면)
	@GetMapping("csc/report-list")
	public String getReportList(Model model) {
		
		String no = "1";
		
		List<ReportVo> rList = service.getReportList(no);
		
		model.addAttribute("rList", rList);
		
		return "csc/member/report-list";
	}
	
	// 신고 상세 조회
	@GetMapping("csc/report/detail")
	@ResponseBody
	public ReportVo getReportByNo(String rno) throws Exception {
		
		String no = "1";
		
		if("".equals(rno) || rno == null) {
			throw new Exception("QNA 상세조회 실패");
		}
		
		ReportVo rvo = new ReportVo();
		rvo.setReporter(no);
		rvo.setNo(rno);
		
		
		ReportVo vo = service.getReportByNo(rvo);
		
		if(vo == null) {
			throw new Exception("QNA 상세조회 실패");
		}
		
		return vo;
	}
	
	// 신고 내역 삭제
	@GetMapping("csc/report/delete")
	public String deleteReport(String rno) throws Exception {
		
		String memberNo = "1";
		
		ReportVo rvo = new ReportVo();
		
		rvo.setNo(rno);
		rvo.setReporter(memberNo);
		
		int result = service.deleteReport(rvo);
		
		if(result != 1) {
			throw new Exception("신고 내역 삭제 에러");
		}
		
		return "redirect:/csc/report-list";
	}
	
	//--------------------------------------------------------------------------------------------
	
	// 관리자
	
	// 1대1 상담내역(화면)
	@GetMapping("admin/csc/inquiry-list")
	public String adminInquiryList() {
		return "csc/admin/inquiry-list";
	}
	
	// 신고내역(화면)
	@GetMapping("admin/csc/report-list")
	public String adminReportList() {
		return "csc/admin/report-list";
	}
	
	// FAQ조회(화면)
	@GetMapping("admin/csc/faq")
	public String adminFAQ() {
		return "csc/admin/faq";
	}
	
	// 제제내역 (화면)
	@GetMapping("admin/member/sanction-list")
	public String sanctionList() {
		return "admin/member/sanction-list";
	}
	
	// 회원조회 (화면)
	@GetMapping("admin/member/member-list")
	public String memberList() {
		return "admin/member/member-list";
	}
	

}
