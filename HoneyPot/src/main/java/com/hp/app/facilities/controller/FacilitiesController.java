package com.hp.app.facilities.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.app.admin.vo.AdminVo;
import com.hp.app.innerFac.service.InnerFacService;
import com.hp.app.innerFac.vo.InnerFacImgVo;
import com.hp.app.innerFac.vo.InnerFacRsVo;
import com.hp.app.innerFac.vo.InnerFacVo;
import com.hp.app.member.vo.MemberVo;
import com.hp.app.yerin.functions.YerinFunctions;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Controller 
@RequiredArgsConstructor
@Slf4j
public class FacilitiesController {
	private final InnerFacService service;
	private final YerinFunctions y;

	//예약 화면 보여주기
	//no=1이면 도서관 관련
	//이때에도 화면측으로 현재 날짜의 예약들을 보내주어야함.
	@GetMapping("facilities/library/reserve")
	public String reserve(Model model,HttpSession session) throws Exception {
		int no=1;
		MemberVo loginMember = (MemberVo)session.getAttribute("loginMember");
		String MemberNo = loginMember.getNo();
		
		//편의시설마다 시간이 다르므로 조회해와야함. 
		//이때 최대인원도 같이 조회하면되잖아?
		InnerFacVo fvo = service.getOpenCloseTime(no);
		
		InnerFacRsVo rsVo = new InnerFacRsVo();
		rsVo.setMemberNo(MemberNo);
		
		//현재날짜정보만 String type으로 넘기기
		//날짜를 주면 String으로 format에 맞게 변환해주는 함수 호출
		String date = y.getStringDate(new Date());
		
		rsVo.setReserveTime(date);
		rsVo.setAmenityNo("1");
		//json으로 변환해야함
		List<String> dateList = service.getReservationTimeInfo(rsVo);
		ObjectMapper om = new ObjectMapper();
		String jsonDate = om.writeValueAsString(dateList);
		
		//예린함수를 거쳐서 openTime, closeTime전달...
		int opentime = y.changeInt(fvo.getOpenTime());
		int closetime = y.changeInt(fvo.getCloseTime());
		
		
		
		
		
		
		model.addAttribute("openTime",opentime);
		model.addAttribute("closeTime",closetime);
		model.addAttribute("reservedDate",jsonDate);
		model.addAttribute("maxNum",fvo.getMaxNum());
		
		
		
		return "innerFacilities/makeLibraryReservation";
	}
	
	//도서관 예약시 DB에 insert하기
	@RequestMapping("facilities/reserve")
	public String reserve(String amenityNo, String date, String startTime,HttpSession session,Model model) {
		//값이 잘 전달 되는거 확인 했다
		//전달된 값으로 에약일시 구하기 두 값을 전달하여 합친 예약일시를 구하는 함수 호출 및 메서드 실행ㅇ
		
		String combinedDate = y.getCombinedDate(date,startTime);
		
		//세션에서 꺼내기 loginMember
		MemberVo loginMember = (MemberVo)session.getAttribute("loginMember");
		
		//RsVo에 값들 집어넣기
		InnerFacRsVo rsVo =  new InnerFacRsVo();
		rsVo.setAmenityNo(amenityNo);
		rsVo.setMemberNo(loginMember.getNo());
		rsVo.setReserveTime(combinedDate);
		
		//rsVo를 전달하면서 service호출
		int result = service.makeReservation(rsVo);
		
		if(result!=1) {
			throw new RuntimeException("rsVo를 DB에 전달하는 과정에서 에러발생");
		}
		
		//예약이 완료되었다는 표시를 해줘야하기 때문에 Model에 값을 넣어 전달 모델에 해당 시작시간을 넣자
		model.addAttribute("selectedTime",startTime);
		
		
		
		return "redirect:/facilities/library/reserve?no=1";
		
	}
	
	//ajax로 날짜가 바뀔 때마다 요청받았을 때 
	@GetMapping(produces ="application/json; charset=UTF-8",value = "innerFac/reserve/reservationInfo")
	@ResponseBody
	public String getReservationInfoByNo(String date,HttpSession session,HttpServletResponse resp) throws Exception {
		
		//세션에서 loginMember꺼내지는지
		MemberVo loginMember = (MemberVo)session.getAttribute("loginMember");
		
		String memberNo = loginMember.getNo();
		
		//조건에 해당하는 날짜값들의 List를 service를 호출해서 받아오기
		//값을 담아서 주자  InnerFacRsVo에 값을 담아서 주면 되잖아.
		//받아온 date의 형식을 확인
		
		//date에서 '-'문자 제거
		
		// removedDate를 rsvo에 담기
		InnerFacRsVo rsVo =  new InnerFacRsVo();
		rsVo.setReserveTime(date);
		rsVo.setMemberNo(memberNo);
		
		//서비스 호출하면서 rsVo넘기고 return은 list로 받아오기
		List<String> dateList =  service.getReservationTimeInfo(rsVo);
		
		//jackson으로 list를 json형식으로 바꿔서 내보내기
		ObjectMapper om = new ObjectMapper();
		String jsonStr = om.writeValueAsString(dateList);
		
		//이러면 list가 json형태로 잘 전달되서 log로 찍히겠지? 
		return jsonStr;
	}
	
	//ajax로 get요청을 받았을 때 선택한 시간에 따라 인원수를 화면측으로 전달
	//json형식이여햐함.
	@GetMapping("innerFac/reserve/reservedPeopleCnt")
	@ResponseBody
	public String getNumberOfPeople(@RequestParam Map<String,String> paramMap) throws Exception {		
		//예린함수를 통해 date와 time을 넘기면 String date를 반환하는 함수 호출
		String date = y.getCombinedDate(paramMap.get("date"), paramMap.get("time"));
		Map<String,String> map = new HashMap<String, String>();
		map.put("reserveTime", date);
		map.put("amenityNo", paramMap.get("amenityNo"));
		//map을 전달하면서 서비스를 호출 int를 반환
		int peopleCnt = service.getReservedPeopleCntByTime(map);
		
		String cnt = peopleCnt+"";
		
		ObjectMapper om = new ObjectMapper();
		
		Map<String,String> resultMap = new HashMap<String, String>();
		resultMap.put("cnt", cnt);
		
		String jsonStr = om.writeValueAsString(resultMap);
		return jsonStr;
	}
	@RequestMapping("innerFac/delete")
	public String cancelReservation(String amenityNo, String date, String startTime,HttpSession session,Model model) {
		MemberVo loginMember = (MemberVo)session.getAttribute("loginMember");
		
		//상태값을 변경해주는 걸로다가..
		String dateStr = y.getCombinedDate(date, startTime);
		//rsVo
		InnerFacRsVo rsVo = new InnerFacRsVo();
		rsVo.setAmenityNo(amenityNo);
		rsVo.setReserveTime(dateStr);
		rsVo.setMemberNo(loginMember.getNo());
		log.info("값이 잘넘어왔는지 확인좀...rsvo : {}",rsVo);
		int result = service.delete(rsVo);
		
		return "redirect:/facilities/library/reserve?no=1";
	}
	
//	//주변시설지도화면
//	@GetMapping("facilities/outerFacilities/map")
//	public String showMap() {
//		return "outerFacilities/showMap";
//	}
	
//	//본인이 남긴 리뷰화면
//	@GetMapping("facilities/outer/review-list")
//	public String reviewList() {
//		return "mypage/act/reviewOuterFacilities";
//	}
	
	//관리자 편의시설 정보변경(화면) //관리소장만 접근가능함
	@GetMapping("/admin/innerFac/editInfo")
	public String showEditInfoPage(int facNo,Model model,HttpSession session) {
		AdminVo loginAdmin = (AdminVo)session.getAttribute("loginAdmin");
		String adminName = loginAdmin.getName();
		
		if(!adminName.equals("관리소장")) {
			throw new RuntimeException();
		}
		//조회니까 값을 받아와야함.
		//이미있음
		//List<InnerFacVo> facVoList = service.getAllFacInfo(facNo);
		InnerFacVo fvo = service.getInnerFacInfo(facNo);

		
		model.addAttribute("facVo",fvo);
		//facNo에 따라 다른곳으로 포워딩
		switch (facNo) {
		case 1 : return "admin/facilities/library-editInfo"; //1번은 도서관
		case 2 : return "";//수영장;
		case 3 : return "";//헬스장;
		case 4 : return "";//골프장;
		default: return "admin/facilities/library-editInfo";
		}
	}
	
	//정보변경 요청 form
	@PostMapping("/admin/innerFac/modifyInfo")
	public String editInfo(String facNo, @RequestParam Map<String,String> infoMap) {
		
		log.info("infoMap : {}",infoMap);
		
		int result = service.updateInnerFacInfo(infoMap) ;
		
		//facNo에 따라 다른곳으로 포워딩
		switch (facNo) {
		case "1" : return "redirect:/innerFac/info?no=1"; //1번은 도서관
		case "2" : return "";//수영장;
		case "3" : return "";//헬스장;
		case "4" : return "";//골프장;
		default: return "redirect:/innerFac/info?no=1"; 
		}
		
	}
	
	//편의시설 공통 사진조회및 변경(화면)-사진DB에서 조회
	@GetMapping("/admin/innerFac/editImg")
	public String editInnerFacImg(String facNo,Model model) {
		List<InnerFacImgVo> imgVoList = service.getInnerFacImgList(facNo);
		
		//시설번호에따라 다름 
		model.addAttribute("imgVoList",imgVoList);
		//facNo에 따라 다른곳으로 포워딩
		switch (facNo) {
		case "1" : return "admin/facilities/library-editIMG"; //1번은 도서관
		case "2" : return "";//수영장;
		case "3" : return "";//헬스장;
		case "4" : return "";//골프장;
		default: return "facilities/library-editIMG"; 
		}
	}
	
	//ajax로 받은 이미지 서버에 올리고 DB에 사진제목 올리기
	@RequestMapping("/admin/innerFac/modifyImg")
	public String modifyInnerFacImg(@RequestParam("file") MultipartFile multi,@RequestParam String facNo,Model model,HttpServletRequest req) throws Exception{
		log.info("ajax로 받은 정보확인 facNo: {}",facNo);
		String uploadpath =  req.getServletContext().getRealPath("/resources/innerFac/");
		String originFilename = multi.getOriginalFilename();
		log.info("파일 잘넘어왔는지 : {}",originFilename);	
		
		String path = uploadpath+originFilename;
		
		File target = new File(path);
		
		multi.transferTo(target);
		
		//db에 오리진네임 insert하는 과정...
		//맵준비해서 dao까지 넘기기
		Map<String,String> infoMap = new HashMap<String, String>();
		infoMap.put("facNo", facNo);
		infoMap.put("originName", originFilename);
		
		//db에 이미지명 추가
		int result = service.addInnerFacImg(infoMap);

		
		return "redirect:/admin/innerFac/editImg?facNo="+facNo;
	}
	
//	//관리자 편의시설 관리
//	@GetMapping("facilities/admin/reserve-list")
//	public String manageReservation() {
//		return "admin/facilities/reserveList";
//	}
	
	//도서관 시설소개 (화면)
	@GetMapping("innerFac/info")
	public String innerFacInfo(int no,Model model) {
		//넘버에따라 시설정보를 조회해서 모델에 정보를 담고 return을 달리하기
		InnerFacVo fvo = service.getInnerFacInfo(no);
		
		//changeName을 담은... 
		List<String> fimgList = service.getInnerFacImg(no);
		
		log.info("fimgList : {}",fimgList);
		
		model.addAttribute("fvo",fvo);
		model.addAttribute("fimgList",fimgList);
		//fimgList의 개수를 model에 담아야함.
		model.addAttribute("endImgCount",fimgList.size());
		
		switch(no) {
			case 1 : return "innerFacilities/showLibraryInfo";
			case 2 : return "innerFacilities/showPoolInfo";
			case 3 : return "innerFacilities/showGymInfo";
			default : return "error-Page";
		}
	}
	
	
	
	
}

