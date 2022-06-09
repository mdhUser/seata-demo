package cn.itcast.account.service;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

/**
 * @description:
 * @author: Maxwell
 * @email: maodihui@foxmail.com
 * @date: 2022/6/9 19:26
 */
@LocalTCC
public interface TCCAccountService {

    /**
     * 从用户账户中扣款
     *
     * @param userId
     * @param money
     */
    @TwoPhaseBusinessAction(name = "deduct", commitMethod = "confirm", rollbackMethod = "cancel")
    void deduct(@BusinessActionContextParameter(paramName = "userId") String userId,
                @BusinessActionContextParameter(paramName = "money") int money);

    /**
     * confirm: 提交
     * @param context 业务上下文对象
     */
    void confirm(BusinessActionContext context);

    /**
     *  事务补偿操作
     * @param context
     */
    void cancel(BusinessActionContext context);


}
