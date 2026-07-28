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
    } else {
        $('#inviteHint').text('\u8bf7\u586b\u5199\u5458\u5de5\u4fe1\u606f\u5b8c\u6210\u6ce8\u518c\uff1b\u8bf7\u8f93\u5165\u516c\u53f8\u6216\u90e8\u95e8\u63d0\u4f9b\u7684\u6ce8\u518c\u9080\u8bf7\u7801');
    }
}

function resolveInvite(token, code) {
    $.get(ctx + "register/invite/resolve", {
        inviteToken: token || '',
        inviteCode: code || ''
    }, function(r) {
        if (r.code == web_status.SUCCESS && r.data) {
            if (r.data.valid) {
                $('#orgRow, #orgCompanyGroup, #orgDeptGroup').show();
                $('#companyName').text(r.data.companyName || '-');
                $('#deptName').text(r.data.deptName || '-');
                var modeText = r.data.registerMode === '0' ? '\u81ea\u52a8\u901a\u8fc7' : '\u9700\u4eba\u5de5\u5ba1\u6838';
                $('#inviteHint').text('\u9080\u8bf7\u6709\u6548\uff08' + modeText + '\uff09\uff0c\u516c\u53f8/\u90e8\u95e8\u5df2\u9501\u5b9a');
                if (token) {
                    $('#inviteCodeGroup').hide();
                } else {
                    $('#inviteCode').prop('readonly', true);
                    $('.invite-tip').hide();
                }
            } else {
                $('#orgRow, #orgCompanyGroup, #orgDeptGroup').hide();
                $('#inviteCode').prop('readonly', false);
                $('#inviteCodeGroup').show();
                $('.invite-tip').show();
                $('#inviteHint').text(r.data.message || '\u9080\u8bf7\u65e0\u6548\u6216\u5df2\u8fc7\u671f\uff0c\u8bf7\u68c0\u67e5\u9080\u8bf7\u7801');
            }
        } else {
            $('#orgRow, #orgCompanyGroup, #orgDeptGroup').hide();
            $('#inviteHint').text((r && r.msg) || '\u9080\u8bf7\u89e3\u6790\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u9080\u8bf7\u7801');
        }
    }).fail(function() {
        $('#inviteHint').text('\u9080\u8bf7\u89e3\u6790\u5931\u8d25\uff0c\u8bf7\u7a0d\u540e\u91cd\u8bd5');
    });
}

function register() {
    var userName = $.common.trim($("input[name='userName']").val());
    var phonenumber = $.common.trim($("input[name='phonenumber']").val());
    var password = $.common.trim($("input[name='password']").val());
    var confirmPassword = $.common.trim($("input[name='confirmPassword']").val());
    var validateCode = $("input[name='validateCode']").val();
    var inviteToken = $('#inviteToken').val();
    var inviteCode = $.common.trim($('#inviteCode').val());

    if ($.common.isEmpty(inviteToken) && $.common.isEmpty(inviteCode)) {
        $.modal.msg("\u8bf7\u8f93\u5165\u6709\u6548\u7684\u90e8\u95e8\u9080\u8bf7\u7801");
        return false;
    }
    if ($.common.isEmpty(validateCode) && captchaEnabled) {
        $.modal.msg("\u8bf7\u8f93\u5165\u56fe\u7247\u9a8c\u8bc1\u7801");
        return false;
    }
    if (password !== confirmPassword) {
        $.modal.msg("\u4e24\u6b21\u5bc6\u7801\u4e0d\u4e00\u81f4");
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
            "inviteToken": inviteToken,
            "inviteCode": inviteCode,
            "validateCode": validateCode
        },
        beforeSend: function () {
            $.modal.loading($("#btnSubmit").data("loading"));
        },
        success: function(r) {
            if (r.code == web_status.SUCCESS) {
                var tip = r.msg || "\u6ce8\u518c\u6210\u529f";
                layer.alert("<font color='red'>" + tip + "</font>", {
                    icon: 1,
                    title: "\u7cfb\u7edf\u63d0\u793a"
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
            password: { required: true, minlength: 5, maxlength: 20 },
            confirmPassword: { required: true, equalTo: "[name='password']" },
            inviteCode: {
                required: function() {
                    return !$.common.trim($('#inviteToken').val());
                }
            }
        },
        messages: {
            userName: { required: icon + "\u8bf7\u8f93\u5165\u59d3\u540d" },
            phonenumber: { required: icon + "\u8bf7\u8f93\u5165\u624b\u673a\u53f7" },
            password: {
                required: icon + "\u8bf7\u8f93\u5165\u5bc6\u7801",
                minlength: icon + "\u5bc6\u7801\u4e0d\u80fd\u5c11\u4e8e5\u4e2a\u5b57\u7b26"
            },
            confirmPassword: {
                required: icon + "\u8bf7\u518d\u6b21\u8f93\u5165\u5bc6\u7801",
                equalTo: icon + "\u4e24\u6b21\u5bc6\u7801\u4e0d\u4e00\u81f4"
            },
            inviteCode: { required: icon + "\u8bf7\u8f93\u5165\u90e8\u95e8\u9080\u8bf7\u7801" }
        },
        submitHandler: function(form) {
            register();
        }
    })
}
