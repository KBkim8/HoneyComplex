package com.hp.app.innerFac.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.hp.app.innerFac.vo.InnerFacRsVo;
import com.hp.app.innerFac.vo.InnerFacVo;

public interface InnerFacDao {
	//리스트로 담을까 그냥 vo에 담기만할까? 모두다 조회해서 시간만 리턴하면 되는거 아님?
	List<InnerFacVo> getAllInnerFacInfo(SqlSessionTemplate sst);
	InnerFacVo getOpenCloseTime(SqlSessionTemplate sst,int no);
	int makeReservation(SqlSessionTemplate sst, InnerFacRsVo rsVo);
}
