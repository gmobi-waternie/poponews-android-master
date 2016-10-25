package com.gmobi.poponews.provider;

import com.momock.service.IService;

/**
 * User: vivian .
 * Date: 2016-08-04
 * Time: 09:45
 */
public interface IDataProvider extends IService{
    boolean getData();
    void initData();
}
