package com.hp.app.calendar.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;

import com.hp.app.calendar.vo.AllCalendarVo;
import com.hp.app.calendar.vo.NoticeCalendarVo;

public interface CalendarDao {
	
	int regiMemberSchedule(SqlSessionTemplate sst, Map<String, String> regiInfoMap);

	int regiAdminSchedule(SqlSessionTemplate sst, Map<String, String> regiInfoMap);

	List<NoticeCalendarVo> getNoticeCal(SqlSessionTemplate sst, Map<String, String> infoMap);

	List<AllCalendarVo> getAllNoticeCalByDong(SqlSessionTemplate sst, String dongName);

	List<AllCalendarVo> getAllMemberCalByPerson(SqlSessionTemplate sst, String memberNo);
	
}
