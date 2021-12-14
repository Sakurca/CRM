layui.use(['form','jquery','jquery_cookie','layer'], function () {
    var form = layui.form,
        layer = layui.layer,
        $ = layui.jquery,
        $ = layui.jquery_cookie($);


    //监听提交
    form.on('submit(login)', function(data){
        //获取表单元素的值（用户名+密码）
        var fieldData = data.field;

        //判断参数是否为空
        if (fieldData.username == "undefined" || fieldData.username.trim()==''){
            layer.msg("用户名不能为空")
            return;
        }
        if(fieldData.password == "undefined" || fieldData.password.trim()==''){
            layer.msg("密码不能为空！")
            return;
        }

        $.ajax({
            type:"post",
            url:ctx+"/user/login",
            data:{
                "userName":fieldData.username,
                "userPwd":fieldData.password
            },
            //返回的数据格式
            dataType:"json",
            success:function (msg){
                //判断是否登录成功
                if(msg.code == 200){
                    //成功提示
                    layer.msg("登录成功！",function (){
                        //将用户的数据存储到Cookie
                        $.cookie("userIdStr",msg.result.userIdStr);
                        $.cookie("userName",msg.result.userName);
                        $.cookie("userPwd",msg.result.trueName);

                        //记住我
                        if($("input[type='checkbox']").is(":checked")){
                            $.cookie("userIdStr",msg.result.userIdStr,{expires:7});
                            $.cookie("userName",msg.result.userName,{expires:7});
                            $.cookie("userPwd",msg.result.trueName,{expires:7});
                        }
                        //跳转
                        window.location.href=ctx+"/main";
                    });

                }else {
                    //失败提示
                    layer.msg(msg.msg)
                }
            }
        });
        //取消默认行为
        return false;
    });

});