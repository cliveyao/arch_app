<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html>
<head>
<title>管理登录</title>
</head>
<body>
	<div class="row">
		<div class="col-md-6">
			<!-- BEGIN LOGIN FORM -->
			<form id="login-form" class="login-form" action="${ctx}/admin/login" method="post">
				<%--End management login ID --%>
				<input type="hidden" name="source" value="A" />
				<h3 class="form-title" style="color: #666666">system login</h3>
				<div class="form-group">
					<!--ie8, ie9 does not support html5 placeholder, so we just show field title for that-->
					<label class="control-label visible-ie8 visible-ie9">Login account</label>
					<div class="input-icon">
						<i class="fa fa-user"></i> <input class="form-control placeholder-no-fix" type="text" autocomplete="off"
							placeholder="Login account" name="username" value="${auth_username_value}" required="true" data-msg-required="请填写登录账号" />
					</div>
				</div>
				<div class="form-group">
					<label class="control-label visible-ie8 visible-ie9">login password</label>
					<div class="input-icon">
						<i class="fa fa-lock"></i> <input class="form-control placeholder-no-fix" type="password" autocomplete="off"
							placeholder = " password " name = "password" required = "true" data-msg-required = " Please fill out the password " / >
					</div>
				</div>
				<c:if test="${auth_captcha_required!=null}">
					<div class="form-group">
						<label class="control-label visible-ie8 visible-ie9">Codes</label>
						<div class="input-group">
							<div class="input-icon">
								<i class="fa fa-qrcode"></i> <input class="form-control captcha-text" type="text" autocomplete="off"
                                placeholder = " codes ... see a clickable image to refresh " name = "captcha" required = "true" data-msg-required = " Please enter the verification code" / >
							</div>
							<span class="input-group-btn" style="cursor: pointer;"> <img alt="Codes" class="captcha-img"
								src="${ctx}/assets/img/captcha_placeholder.jpg" title="See ? Click Refresh" />
							</span>
						</div>
					</div>
				</c:if>
				<c:if test="${error!=null}">
					<div align='center' class='alert alert-danger'>${error}</div>
				</c:if>
				<div class="form-actions">
					<label> <input type="checkbox" name="rememberMe" checked="true" value="true" /> Remember me , Remember login
					</label>
					<button type="submit" class="btn blue pull-right">
						log in<i class="m-icon-swapright m-icon-white"></i>
					</button>
				</div>
				<div class="forget-password">
					<div class="row">
						<div class="col-md-3">
							<c:if test="${casSupport}">
								<p>
									<a href='<s:property value="casRedirectUrl"/>'>sign in</a>
								</p>
							</c:if>
						</div>
						<div class="col-md-9">
							<p class="pull-right">
								forgot password? <a href="${ctx}/admin/password/forget" data-toggle="modal-ajaxify" title="Forgot password" data-modal-size="550px">Forgot password</a>
								<c:if test="${mgmtSignupEnabled}">
                                &nbsp; &nbsp;&nbsp; &nbsp; No account ? <a href="${ctx}/admin/signup"
										data-toggle="modal-ajaxify" title="自助注册">Self- registration</a>
								</c:if>
							</p>
						</div>
					</div>
				</div>
			</form>
			<!-- END LOGIN FORM -->
		</div>
		<div class="col-md-1"></div>
		<div class="col-md-5">
			<div class="form-info" style="height: 270px; margin-top: 50px">
				<H4> prompt access </ h4>
                <P> recommend using the latest version of Firefox or Chrome browser to access the application in order to avoid unnecessary browser compatibility issues. </ P>
				<c:if test="${cfg.dev_mode}">
					<p id="devModeTips" style="padding: 10px">
						<b> Development / test / demo Login Quick Entrance : <br /> <br /> <a href="javascript:void(0)" onclick="setupDevUser('admin','admin123')">admin超级管理员(admin/admin123)</a>
						</b>
					</p>
					<script type="text/javascript">
                        var $form = $("#login-form");

                        $("input[name='username']", $form).val("admin");
                        $("input[name='password']", $form).val("admin123");

                        function setupDevUser(user, password) {
                            $("input[name='username']", $form).val(user);
                            $("input[name='password']", $form).val(password);
                            $form.submit();
                        }
                        jQuery(document).ready(function() {
                            $("#devModeTips").pulsate({
                                color : "#bf1c56",
                                repeat : 10
                            });
                        });
                    </script>
				</c:if>
			</div>
		</div>
	</div>

	<script src="${ctx}/assets/admin/app/login.js"></script>
</body>
</html>