package cn.itcast.account.service.impl;


import cn.itcast.account.entity.AccountFreeze;
import cn.itcast.account.mapper.AccountFreezeMapper;
import cn.itcast.account.mapper.AccountMapper;
import cn.itcast.account.service.TCCAccountService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * @description:
 * @author: Maxwell
 * @email: maodihui@foxmail.com
 * @date: 2022/6/9 20:53
 */
@Slf4j
@Service
public class TCCAccountServiceImpl implements TCCAccountService {


    private final AccountMapper accountMapper;

    private final AccountFreezeMapper accountFreezeMapper;

    public TCCAccountServiceImpl(AccountMapper accountMapper, AccountFreezeMapper accountFreezeMapper) {
        this.accountMapper = accountMapper;
        this.accountFreezeMapper = accountFreezeMapper;
    }


    /**
     * try逻辑
     *
     * @param userId
     * @param money
     */
    @Override
    public void deduct(String userId, int money) {
        //从全局事务中获取全局id
        String xid = RootContext.getXID();
        AccountFreeze accountFreeze = accountFreezeMapper.selectById(xid);
        //如果cancel已经执行，如果已经执行cancel则不执行try逻辑
        if (!ObjectUtils.isEmpty(accountFreeze)) {
            //删除冻结记录
            accountFreezeMapper.deleteById(xid);
            return;
        }
        accountFreeze.setXid(xid);
        accountFreeze.setFreezeMoney(money);
        accountFreeze.setUserId(userId);
        accountFreeze.setState(AccountFreeze.State.TRY);
        accountFreezeMapper.insert(accountFreeze);
        accountMapper.deduct(userId, money);
    }

    @Override
    public void confirm(BusinessActionContext context) {
        String xid = context.getXid();
        //根据xid删除冻结记录
        accountFreezeMapper.deleteById(xid);
    }

    @Override
    public void cancel(BusinessActionContext context) {
        String xid = context.getXid();
        String userId = context.getActionContext("userId").toString();
        int money = Integer.parseInt(context.getActionContext("money").toString());
        AccountFreeze accountFreeze = accountFreezeMapper.selectById(xid);
        if (ObjectUtils.isEmpty(accountFreeze)) {
            //try需要知道cancel之前有没有被执行，设置status为2 进行业务悬挂
            accountFreeze=new AccountFreeze();
            accountFreeze.setXid(xid);
            accountFreeze.setFreezeMoney(0);
            accountFreeze.setUserId(userId);
            accountFreeze.setState(AccountFreeze.State.CANCEL);
            accountFreezeMapper.insert(accountFreeze);
            return;
        }
        accountFreeze.setFreezeMoney(0);
        accountFreeze.setState(AccountFreeze.State.CANCEL);

        //恢复数据
        accountMapper.refund(userId, money);
    }
}