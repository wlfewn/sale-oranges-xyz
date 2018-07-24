//package com.github.geng.auth.center.entity;
//
//import com.github.geng.entity.BaseLongIdTimeEntity;
//import lombok.Data;
//
//import javax.persistence.*;
//
// 微服务之间调用不配置token验证
///**
// * 服务访问白名单
// * @author geng
// */
//@Data
//@Entity
//@Table(name = "service_allow_clients")
//// 注意@Table与@Entity区别，参考 https://www.cnblogs.com/alsf/p/7822241.html
//public class AllowClient extends BaseLongIdTimeEntity {
//    private static final long serialVersionUID = 1L;
//
//    private Long clientId;              // 可访问的服务id
//    @Column(name = "is_locked")
//    private boolean locked;             // 是否禁止访问
//    private String clientName;          // 可访问服务名称
//    private Long creatorId;             // 创建人id
//    private Long modifierId;            // 修改人id
//    private String creator;             // 创建人名称
//    private String modifier;            // 修改人名称
//
//    @ManyToOne
//    @JoinColumn(name = "service_id")
//    private ServiceClient client;       // 关联的服务记录
//
//}
