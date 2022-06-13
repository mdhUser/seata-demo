package cn.itcast.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Flippy
 */
@FeignClient("account-service")
public interface AccountClient {
    @RequestMapping("/account/{userId}/{money}")
    void deduct(@PathVariable("userId") String userId, @PathVariable("money") Integer money);
}
