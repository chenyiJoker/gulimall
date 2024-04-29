package com.cy.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.cy.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "9021000136641900";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCWyKC6M0ODUrtH2JgkFflvUrHdCgvX3phclQFnag6SgUVAf5Yc/Hrfqi4HkBXU7Yw5jqVpt8B2jsG4vJHZHGGi6nJqUnp4QJZ1EN4DAtdpWqKcfZ8WmDjfH3iLG75PecKRmTgzvbdFg1cb7465drrIDQ9KIr99IiUHUNO6MpumpMUfjIR5qIhEGGfw8cCxuPCIF+kS3o4coiugj9wRRiXH16ajdxzIY4DJNs0AL6qasm7xiaC3UN1jMBI85y6vWYu438+oooOs+arKMTVYZ+2EEt+SLE9tSI51BBdfRCxqW6Nq708aa1o3cfJdbUIssPwbHwtWhGsHAVoLpegDqOLdAgMBAAECggEAdsq5VfjtR5j2UzQhKMznJv1Pg4VWr0t/XfFGmeLEeEe/94CNE2kWpP++I9cwXACKaQ4o45EQ4Fa36IMnpJTnlyJ94aX5NqQr5S7UuiqS6z2svv15SKPEqoRww6en15Vl5K+Bx8G/kdRX3HpvR05y+QjQ8udNVZiA87KKHDeiyTXxIQkcD/ikVZs+gjqcXMVYxfKwh4zISnfd+xTaRe+lchmCKuerjySqZbY7EsPt7nl68Mn6LbwwK14XNCoTCruA5Cj2RguIa1TkUjgcXYmbxWX64aFazDjaMCFoBbDnmkEfVRBGUjdPFQ1noosOl2k9NyK1qlOsusK50nO0thSXQQKBgQDKZuE/IJFGtlgaen7695UEOoyXmww+HotE5RA8lFvV/mahLKBL6MKSDpfIgSav6PZFTySP6k7JYDIbF2E+kJgvSouGXvTsop22e5vu9y62gd7qikmx0iveI1uRSdlTD5wcfpYh/cAywg1KRp+FxrWNnRcqRee21Vrmma8XeSiLKwKBgQC+tnnxDchhS27PQpmmwGYi/CxoGxjq46vr9CBmUP8KNIsFSJ6ydMTmvpWnFFcd02nt7UIvYlstR9wUF1ERakkJxSW/LZYJUmeeHl4lquRdQDsYdDCx2OR9qVcrX6BpICisBPFa8+eEsKusiCdfulnkN5XlmfdsVADJLQm9NxQmFwKBgDVgSjh+mzE9tyBMe54tmoPB+P+mKOVc4rdpfQ2sxkc5ztDkVuWN3XntFUQQIyay1fdTNr4pOVvL3YrdcIyyTx/xlbf65LZe8B6GnIm5b/07ZJEPpP2bAmyTgHthMUnBTRcKnwcZxwzbteuHklwSGiDdc5V3V/MUcuNrVgxnrDR/AoGBAKNv9ijiyuq2nov4Y31IPT+WJOhC8KO74UYgvPNyTl8MkGdCyN3evC+S/OVmxRHGcG16etHo1IKFptGA+fSWxWkeDy87fe0XNb2IAN7/CR3xQbO/P0vquEYIra96EBqHeNOSmNR9WdeZIEaHmFateQPfrxJJVIT9qH6/ni0N2rKZAoGAO1RZwdywUG05MoeHp9CkEkxntHsAJAejl3/5fgu0q2jwJqe680lIdkX65Dc7LmTHNjEM793zV+7XNFlGlQBJbME7jVEcYxenlWCL7Hn7LgvD8JZ+/ue4TaQDZZ0CItQskrEqbXYYJZrQPk/YFWkjBZn7Qg2LFajlDcbaGxOSJgk=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsi94b5kwjO9yucDc5eDKj1Dw10bScoGKL9O1DhMklp0od5A2vjrXPY5PZp5zIHOUrmPZGIO1pv//SVirTaXrC7G+yn+Hkn0sokQwbcY92fPC0JvVDVAtqlucx16fQxORFxObIoom/38BwMXdJhjP1tFK1qlIHltvxtpCl17BAWaXg7boueQqCMHjRkSQmvT8UJAl1fViowKZb4+LLLaJPSiH5sxUQc8ey1KkD9p6yJ7f95f6JRpkv9qQ2C37P8Q47+wnvQNnI/mAdDwBC6Id1nr0z+DQBC1Oza2eiqbcMOYNIzB9Vwk4bZzt/fvPtIhV+rdFcpKDGPT0txtT1WahawIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://order.gulimall.com/order/pay/alipay/success";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.gulimall.com/memberOrder.html";

//    alipay.notify_url=http:xb6cg22x28.52http.net/order/pay/alipay/success
//    alipay.return_url=http:localhost:2000/pay/success.html

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关地址 https://openapi-sandbox.dl.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }
}
