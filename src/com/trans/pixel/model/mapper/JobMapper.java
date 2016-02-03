package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.JobBean;

public interface JobMapper {
	
	public JobBean queryById(long jobId);
	
	public List<JobBean> queryAll();
	
	public int addNewJob(JobBean job);
	
	public int updateJob(JobBean job);
}
