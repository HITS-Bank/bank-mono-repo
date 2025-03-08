//package com.bank.hits.bankcreditservice.config;
//
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//public class JmsConfig {
//
////    @Value("${javax.net.ssl.keyStore}")
////    private String keyStore;
////
////    @Value("${javax.net.ssl.keyStorePassword}")
////    private String keyStorePassword;
////
////    @Value("${javax.net.ssl.trustStore}")
////    private String trustStore;
////
////    @Value("${javax.net.ssl.trustStorePassword}")
////    private String trustStorePassword;
////
////    @Value("${ibm.mq.queueManager}")
////    private String queueManager;
////
////    @Value("${ibm.mq.port}")
////    private Integer port;
////
////    @Value("${ibm.mq.host}")
////    private String host;
////
////    @Value("${ibm.mq.channel}")
////    private String channel;
////
////    @Value("${ibm.mq.connName}")
////    private String connName;
////
////    @Value("${ibm.mq.ssl-cipher-suite}")
////    private String cipherSuite;
////
////    @Value("${ibm.mq.user}")
////    private String user;
////
////    @Value("${ibm.mq.password}")
////    private String password;
////
////
////    @Bean
////    public ConnectionFactory mqConnectionFactory() throws JMSException {
////        MQConnectionFactory mqConnectionFactory = new MQConnectionFactory();
////        mqConnectionFactory.setHostName(host);
////        System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", "false");
////        System.setProperty("javax.net.ssl.keyStore", keyStore);
////        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
////        System.setProperty("javax.net.ssl.trustStore", trustStore);
////        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
////        mqConnectionFactory.setPort(port);
////        mqConnectionFactory.setQueueManager(queueManager);
////        mqConnectionFactory.setChannel(channel);
////        mqConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
////        mqConnectionFactory.setStringProperty(WMQConstants.USERID, user);
////        mqConnectionFactory.setStringProperty(WMQConstants.PASSWORD, password);
////        mqConnectionFactory.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, cipherSuite);
////        return mqConnectionFactory;
////    }
//
//}
