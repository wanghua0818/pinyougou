package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {
    private static final String REDIS_CART_LIST = "cart_list";
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayLogMapper payLogMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbOrder order) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(order.get***())){
            criteria.andLike("***", "%" + order.get***() + "%");
        }*/

        List<TbOrder> list = orderMapper.selectByExample(example);
        PageInfo<TbOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public String addOrder(TbOrder order) {
        String outTradeNo = "";
        //1、获取用户对应的购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(REDIS_CART_LIST).get(order.getUserId());

        //2、遍历购物车列表的每个购物车对应生成一个订单和多个其对应的订单明细
        if (cartList != null && cartList.size() > 0) {
            //本次支付总金额
            double totalFee = 0.0;
            //订单的支付总金额
            double payment = 0.0;
            //本次交易的所有订单
            String orderIds = "";
            for (Cart cart : cartList) {
                //订单基本
                TbOrder tbOrder = new TbOrder();
                long orderId = idWorker.nextId();
                tbOrder.setOrderId(orderId);
                tbOrder.setCreateTime(new Date());
                tbOrder.setUpdateTime(tbOrder.getCreateTime());
                tbOrder.setSourceType(order.getSourceType());
                tbOrder.setReceiver(order.getReceiver());
                tbOrder.setReceiverAreaName(order.getReceiverAreaName());
                tbOrder.setReceiverMobile(order.getReceiverMobile());
                tbOrder.setUserId(order.getUserId());
                //1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价',
                tbOrder.setStatus("1");
                tbOrder.setSellerId(cart.getSellerId());
                tbOrder.setPaymentType(order.getPaymentType());
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    //订单明细
                    orderItem.setId(idWorker.nextId());
                    orderItem.setOrderId(tbOrder.getOrderId());
                    //累计订单的总金额
                    payment += orderItem.getTotalFee().doubleValue();
                    //保存订单明细
                    orderItemMapper.insertSelective(orderItem);
                }
                tbOrder.setPayment(new BigDecimal(payment));
                //累计本次支付的总金额 --支付日志
                totalFee += payment;

                //累加订单的id
                if (orderIds.length() > 0) {
                    orderIds += "," + tbOrder.getOrderId();
                } else {
                    orderIds = tbOrder.getOrderId().toString();
                }
                orderMapper.insertSelective(tbOrder);
            }
            //3、如果是微信支付的话则需要生成支付日志保存到数据库
            if ("1".equals(order.getPaymentType())) {
                TbPayLog payLog = new TbPayLog();
                outTradeNo = idWorker.nextId() + "";
                payLog.setOrderList(orderIds);
                payLog.setCreateTime(new Date());
                payLog.setTotalFee((long) (totalFee * 100));
                payLog.setUserId(order.getUserId());
                payLog.setOutTradeNo(outTradeNo);
                payLog.setTradeState("0");
                payLog.setPayType("1");
                payLogMapper.insertSelective(payLog);
            }
            //4、删除用户对应的购物车列表
            redisTemplate.boundHashOps(REDIS_CART_LIST).delete(order.getUserId());

        }
        //5、返回支付日志 id；如果不是微信支付则返回空
        return outTradeNo;
    }

    //根据支付日志id,查找支付日志信息
    @Override
    public TbPayLog findPayLogByOutTradeNo(String outTradeNo) {
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(outTradeNo);
        return payLog;
    }

    @Override
    public void updateOrderStatus(String outTradeNo, String transactionId) {
        TbPayLog payLog = findPayLogByOutTradeNo(outTradeNo);
        payLog.setTradeState("1");//已支付
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transactionId);
        payLogMapper.updateByPrimaryKeySelective(payLog);
        //2.更新支付日志中对应的每一笔订单的支付状态
        String[] orderIds = payLog.getOrderList().split(",");
        TbOrder order = new TbOrder();
        order.setPaymentTime(new Date());
        order.setStatus("2");
        Example example = new Example(TbOrder.class);
        example.createCriteria().andIn("orderId", Arrays.asList(orderIds));
        orderMapper.updateByExampleSelective(order, example);
    }
}
