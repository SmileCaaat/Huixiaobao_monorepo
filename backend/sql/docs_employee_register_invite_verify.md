# 员工注册邀请验证清单

1. 执行 `upgrade_employee_register_invite.sql`（加手机号唯一索引前先查重）
2. 菜单权限：registerQrcode view/download/manage、registerRecord、user audit
3. 部门管理（非根节点）→ 生成注册二维码（自动通过模式）→ 复制链接
4. `sms.enabled=true`、`sms.provider=log`；从调试日志读取验证码
5. PC `/register?inviteToken=...` → `audit_status=0`、`dispatchable=1`、角色 `maintenance_member`
6. 无邀请注册 → 待审；可登录；不可作为报修/任务处理人
7. 注册记录页通过/驳回；并发审核提示已由他人处理
8. 小程序携带 inviteToken/smsCode 注册；任务空状态文案正确
9. 重新生成会作废旧 Token；过期提示正常
