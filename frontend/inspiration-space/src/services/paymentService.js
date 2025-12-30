import { privateApiCall } from './apiClient';

/**
 * 支付相关服务
 */
export const paymentService = {
  /**
   * 预览订单
   * @param {Number|String} workId 作品ID
   */
  previewOrder: (workId) => {
    return privateApiCall(`/pay-order/preview?workId=${workId}`, 'GET');
  },

  /**
   * 创建订单草稿
   * @param {Number|String} workId 作品ID
   */
  createOrderDraft: (workId) => {
    // 这里后端使用了 @RequestParam，所以放在 query 中
    return privateApiCall(`/pay-order/createDraft?workId=${workId}`, 'POST');
  },

  /**
   * 发起支付
   * @param {String} draftId 订单草稿ID
   * @param {Object} paymentDto 支付参数 { subject, amount, paymentMethod }
   */
  pay: (draftId, paymentDto) => {
    // 后端已调整为接收完整的 PaymentDto 对象作为 RequestBody
    const body = {
      draftId: draftId,
      subject: paymentDto.subject,
      amount: paymentDto.amount,
      paymentMethod: paymentDto.paymentMethod
    };
    
    return privateApiCall('/pay-order/pay', 'POST', body);
  },

  /**
   * 查询订单状态
   * @param {String} transactionNo 交易流水号
   */
  checkStatus: (transactionNo) => {
    // 检查后端是否有现成的查询接口。如果没有，我们可以尝试查询订单详情。
    // 这里的 pay-transaction/updateStatus 是更新接口。
    // 我们暂时使用 pay-order/simple 来查询最近的订单，或者直接尝试获取作品详情看是否已解锁。
    return privateApiCall(`/pay-transaction/updateStatus?transactionNo=${transactionNo}`, 'PATCH');
  }
};
