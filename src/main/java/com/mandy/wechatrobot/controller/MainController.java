package com.mandy.wechatrobot.controller;

import com.mandy.wechatrobot.handle.WeChatMessageHandle;
import com.mandy.wechatrobot.util.Constants;
import com.mandy.wechatrobot.util.MessageUtil;
import com.mandy.wechatrobot.util.WeChatUtil;
import com.mandy.wechatrobot.vo.TextMessage;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Map;

@Controller
@Scope("prototype")
@Namespace("/weChat")
public class MainController extends ActionSupport {

    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory
            .getLogger(MainController.class);

    @Resource(name = "eventMessageHandle")
    private WeChatMessageHandle eventMessageHandle;

    @Resource(name = "textMessageHandle")
    private WeChatMessageHandle textMessageHandle;

    /**
     * 微信的appid
     */
    private static String appid;
    /**
     * 微信的secret
     */
    private String secret;
    /**
     * 关注公众号的微信用户的唯一标识
     */
    private String openid;
    /**
     * 微信传递过来的随机字符串
     */
    private String nonceStr;
    /**
     * 微信验证服务器地址的有效性时传递过来的随机字符串
     */
    private String nonce;
    /**
     * 微信验证服务器地址的有效性时传递过来的回显字符串
     */
    private String echostr;
    /**
     * 信验证服务器地址的有效性时传递过来的时间戳字符串
     */
    private String timestamp;
    /**
     * 微信验证服务器地址的有效性时传递过来的签名字符串
     */
    private String signature;

    /**
     * 接收来至微信服务器的消息
     **/
    @Action("receiveMessage")
    public void receiveMessage() {
        HttpServletResponse response = ServletActionContext.getResponse();
        HttpServletRequest request = ServletActionContext.getRequest();
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = null;
        String fromUserName = null;
        String toUserName = null;
        try {
            out = response.getWriter();
            if (Constants.GET.equals(request.getMethod())) {
                // 验证签名是否有效
                if (WeChatUtil.checkSignature(signature, timestamp, nonce)) {
                    out.write(echostr);
                } else {
                    out.write("");
                }
            } else {
                String result;
                Map<String, String> parameters = MessageUtil.parseXml(request
                        .getInputStream());
                fromUserName = parameters.get("FromUserName");
                toUserName = parameters.get("ToUserName");
                String msgType = parameters.get("MsgType");
                if ("event".equals(msgType)) {
                    result = eventMessageHandle.processMessage(parameters);
                } else if ("text".equals(msgType)) {
                    result = textMessageHandle.processMessage(parameters);
                } else {
                    result = MessageUtil.ObjectToXml(new TextMessage(toUserName, fromUserName, "我只对文字感兴趣[悠闲]"));
                }
                out.write(result);
            }
        } catch (Exception e) {
            logger.error("接收来至微信服务器的消息出现错误", e);
            out.write(MessageUtil.ObjectToXml(new TextMessage(toUserName,
                    fromUserName, "我竟无言以对！")));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public String getAppid() {
        return appid;
    }

    public String getSecret() {
        return secret;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getEchostr() {
        return echostr;
    }

    public void setEchostr(String echostr) {
        this.echostr = echostr;
    }

}
