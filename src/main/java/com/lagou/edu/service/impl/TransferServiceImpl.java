package com.lagou.edu.service.impl;

import com.lagou.edu.annotation.AutowiredAnnotation;
import com.lagou.edu.annotation.ServiceAnnotation;
import com.lagou.edu.annotation.TransactionalAnnotation;
import com.lagou.edu.dao.AccountDao;
import com.lagou.edu.pojo.Account;
import com.lagou.edu.service.TransferService;
import com.lagou.edu.utils.ConnectionUtils;
import com.lagou.edu.utils.TransactionManager;

/**
 * @author 应癫
 */
@ServiceAnnotation(value = "transferService")
public class TransferServiceImpl implements TransferService {

    @AutowiredAnnotation("accountDao")
    private AccountDao accountDao;

    @Override
    @TransactionalAnnotation
    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {

            Account from = accountDao.queryAccountByCardNo(fromCardNo);
            Account to = accountDao.queryAccountByCardNo(toCardNo);

            from.setMoney(from.getMoney()-money);
            to.setMoney(to.getMoney()+money);

            accountDao.updateAccountByCardNo(to);
            int c = 1/0;
            accountDao.updateAccountByCardNo(from);

    }
}
