
$(function() {
    initInviteFromUrl();
    validateRule();
    $('.imgcode').click(function() {
        var url = ctx + "captcha/captchaImage?type=" + captchaType + "&s=" + Math.random();
        $(".imgcode").attr("src", url);
    });
    $('#togglePwd, #togglePwd2').click(function() {
        var input = $(this).siblings('input');
        if (input.attr('type') === 'password') {
            input.attr('type', 'text');
            $(this).removeClass('fa-eye').addClass('fa-eye-slash');
        } else {
            input.attr('type', 'password');
            $(this).removeClass('fa-eye-slash').addClass('fa-eye');
        }
    });
    $('#btnSms').click(function() {
        sendSms();
    });
    $('#inviteCode').on('blur', function() {
        var code = $.common.trim($(this).val());
        if (code) {
            resolveInvite(null, code);
        }
    });
});

function initInviteFromUrl() {
    var params = new URLSearchParams(window.location.search);
    var token = params.get('inviteToken') || params.get('token');
    var code = params.get('inviteCode');
    if (token) {
        $('#inviteToken').val(token);
        resolveInvite(token, null);
    } else if (code) {
        $('#inviteCode').val(code);
        resolveInvite(null, code);
    }
}

function resolveInvite(token, code) {
    $.get(ctx + "register/invite/resolve", {
        inviteToken: token || '',
        inviteCode: code || ''
    }, function(r) {
        if (r.code == web_status.SUCCESS && r.data) {
            if (r.data.valid) {
                $('#orgInfo').show();
                $('#companyName').text(r.data.companyName || '-');
                $('#deptName').text(r.data.deptName || '-');
                var modeText = r.data.registerMode === '0' ? '自动通过' : '需人工审核';
                $('#inviteHint').text('邀请有效（' + modeText + '），公司/部门已锁定');
            } else {
                $('#orgInfo').hide();
                $('#inviteHint').text(r.data.message || '邀请无效，将进入人工审核');
            }
        }
    });
}

function sendSms() {
    var phone = $.common.trim($("input[name='phonenumber']").val());
    if (!phone || !/^1\d{10}$/.test(phone)) {
        $.modal.msg("请输入正确的手机号");
        return;
    }
    var $btn = $('#btnSms');
    $btn.prop('disabled', true);
    $.post(ctx + "register/sms/send", { phonenumber: phone }, function(r) {
        if (r.code == web_status.SUCCESS) {
            $.modal.msgSuccess(r.msg || "验证码已发送");
            var left = 60;
            var timer = setInterval(function() {
                left--;
                $btn.text(left + 's');
                if (left <= 0) {
                    clearInterval(timer);
                    $btn.prop('disabled', false).text('获取验证码');
                }
            }, 1000);
        } else {
            $.modal.msg(r.msg);
            $btn.prop('disabled', false);
        }
    }).fail(function() {
        $btn.prop('disabled', false);
    });
}

function register() {
    var userName = $.common.trim($("input[name='userName']").val());
    var phonenumber = $.common.trim($("input[name='phonenumber']").val());
    var password = $.common.trim($("input[name='password']").val());
    var confirmPassword = $.common.trim($("input[name='confirmPassword']").val());
    var smsCode = $.common.trim($("input[name='smsCode']").val());
    var validateCode = $("input[name='validateCode']").val();
    var inviteToken = $('#inviteToken').val();
    var inviteCode = $.common.trim($('#inviteCode').val());

    if ($.common.isEmpty(validateCode) && captchaEnabled) {
        $.modal.msg("请输入图片验证码");
        return false;
    }
    if (password !== confirmPassword) {
        $.modal.msg("两次密码不一致");
        return false;
    }
    $.ajax({
        type: "post",
        url: ctx + "register",
        data: {
            "loginName": phonenumber,
            "userName": userName,
            "phonenumber": phonenumber,
            "password": password,
            "smsCode": smsCode,
            "inviteToken": inviteToken,
            "inviteCode": inviteCode,
            "validateCode": validateCode
        },
        beforeSend: function () {
            $.modal.loading($("#btnSubmit").data("loading"));
        },
        success: function(r) {
            if (r.code == web_status.SUCCESS) {
                var tip = r.msg || "注册成功";
                layer.alert("<font color='red'>" + tip + "</font>", {
                    icon: 1,
                    title: "系统提示"
                }, function(index) {
                    layer.close(index);
                    location.replace(ctx + 'login');
                });
            } else {
                $.modal.closeLoading();
                $('.imgcode').click();
                $(".code").val("");
                $.modal.msg(r.msg);
            }
        }
    });
}

function validateRule() {
    var icon = "<i class='fa fa-times-circle'></i> ";
    $("#registerForm").validate({
        rules: {
            userName: { required: true },
            phonenumber: { required: true, isPhone: true },
            smsCode: { required: true },
            password: { required: true, minlength: 5, maxlength: 20 },
            confirmPassword: { required: true, equalTo: "[name='password']" }
        },
        messages: {
            userName: { required: icon + "请输入姓名" },
            phonenumber: { required: icon + "请输入手机号" },
            smsCode: { required: icon + "请输入短信验证码" },
            password: {
                required: icon + "请输入密码",
                minlength: icon + "密码不能少于5个字符"
            },
            confirmPassword: {
                required: icon + "请再次输入密码",
                equalTo: icon + "两次密码不一致"
            }
        },
        submitHandler: function(form) {
            register();
        }
    })
}
