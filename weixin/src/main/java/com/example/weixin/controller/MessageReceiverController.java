package com.example.weixin.controller;

import java.io.StringReader;

import javax.xml.bind.JAXB;

import com.example.commons.domain.InMessage;
import com.example.weixin.service.MessageTypeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 控制器 : 负责接收用户的请求参数、调用业务逻辑层代码、返回视图/结果给客户端（浏览器）
// @Controller  基于JSP的控制器
// @RestController 符合RESTful风格的WEB服务的控制器
// RESTful通过不同的请求方法调用不同的处理程序，返回的结果仅仅是数据，不包含视图（HTML、JSP）
@RestController
// 各自写代码的时候，把/kemao_1改为【/拼音名】，用于后面作为路径反向代理的时候区分不同人的代码
// @RequestMapping表示的含义：URL跟控制器的关系映射
@RequestMapping("/lzt_3/weixin/receiver")
public class MessageReceiverController {

	private static final Logger LOG = LoggerFactory.getLogger(MessageReceiverController.class);

	@Autowired
	@Qualifier("inMessageTemplate")
	private RedisTemplate<String, InMessage> inMessageTemplate;

	@GetMapping // 只处理GET请求
	public String echo(//
			@RequestParam("signature") String signature, //
			@RequestParam("timestamp") String timestamp, //
			@RequestParam("nonce") String nonce, //
			@RequestParam("echostr") String echostr//
	) {
		return echostr;
	}

	@PostMapping
	public String onMessage(@RequestParam("signature") String signature, //
			@RequestParam("timestamp") String timestamp, //
			@RequestParam("nonce") String nonce, //
			@RequestBody String xml) {
		LOG.debug("收到用户发送给公众号的信息: \n-----------------------------------------\n"
				+ "{}\n-----------------------------------------\n", xml);


		String type = xml.substring(xml.indexOf("<MsgType><![CDATA[") + 18);
		type = type.substring(0, type.indexOf("]]></MsgType>"));

		Class<InMessage> cla = MessageTypeMapper.getClass(type);

		InMessage inMessage = JAXB.unmarshal(new StringReader(xml), cla);

		LOG.debug("转换得到的消息对象 \n{}\n", inMessage.toString());

		inMessageTemplate.convertAndSend("lzt_3_" + inMessage.getMsgType(), inMessage);


		return "success";
	}
}
