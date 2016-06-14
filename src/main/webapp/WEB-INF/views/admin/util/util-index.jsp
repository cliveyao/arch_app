<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Assistant management functions</title>
</head>
<body>
	<div class="row">
		<div class="col-md-6">
			<div class="portlet box grey">
				<div class="portlet-title">
					<div class="caption">
						<i class="fa fa-reorder"></i> Cache Management
					</div>
					<div class="tools">
						<a class="collapse" href="javascript:;"></a>
					</div>
				</div>
				<div class="portlet-body">
					<form class="form-horizontal form-bordered form-label-stripped form-validation"
						action='${ctx}/admin/util/cache-clear' method="post" data-editrulesurl="false">
						<div class="form-body" style="min-height: 250px">
							<div class="note note-info">
								<P> In order to run the system efficiency , the system will be based on Hibernate and Spring Cache cache data possible support </ p>
					<P> This feature is mainly used to directly modify the database data, the notice to remove the cached data caching framework of the selection thus loading the latest database data . </ P>
						</div>
											</div>
						<div class="form-actions right">
							<button class="btn blue" type="submit">
								<i class="fa fa-check"></i> Empty refresh all cached data
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="portlet box grey">
				<div class="portlet-title">
					<div class="caption">
						<i class="fa fa-reorder"></i> 
Dynamic Update Logger log level
					</div>
					<div class="tools">
						<a class="collapse" href="javascript:;"></a>
					</div>
				</div>
				<div class="portlet-body">
					<form class="form-horizontal form-bordered form-label-stripped form-validation"
						action='${ctx}/admin/util/logger-update' method="post" data-editrulesurl="false">
						<div class="form-body" style="min-height: 250px">
							<div class="note note-info">
									<P> This feature is mainly used in the application process to be dynamically modify Logger log level in order to achieve online Debug debugging system log information for real-time analysis of a number of online troubleshooting problems . </ P>
									<P class = "text-warning"> in the lower log level troubleshooting is completed, it is best to adjust the log level back to the default high level in order to avoid a lot of log information affecting system operation efficiency </ p>							</div>
							<div class="form-group">
								<label class="control-label">Logger Name</label>
								<div class="controls">
									<input type="text" name="loggerName" class="form-control" placeholder="root，Specific package name , specific logger name, etc.">
								</div>
							</div>
							<div class="form-group">
								<label class="control-label">Logger Level</label>
								<div class="controls">
									<select name="loggerLevel">
										<option value="OFF">OFF</option>
										<option value="ERROR">ERROR</option>
										<option value="WARN">WARN</option>
										<option value="INFO">INFO</option>
										<option value="DEBUG">DEBUG</option>
										<option value="TRACE">TRACE</option>
										<option value="ALL">ALL</option>
									</select>
								</div>
							</div>
						</div>
						<div class="form-actions right">
							<button class="btn blue" type="submit">
								<i class="fa fa-check"></i> Dynamic Update Logger log level
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
	<div class="row">
		<div class="col-md-6">
			<div class="portlet box grey">
				<div class="portlet-title">
					<div class="caption">
						<i class="fa fa-reorder"></i> 
Verify load balancing
					</div>
					<div class="tools">
						<a class="collapse" href="javascript:;"></a>
					</div>
				</div>
				<div class="portlet-body">
					<div class="note note-info">
					
<P> provide basic HTTP Request and Session information display pages , for a clustered environment Failover switch between different hosts Check service is normal, check Session replication configuration is effective to achieve a seamless handover of data replication Session property . </ P>					</div>
					<a class="btn blue" href="${ctx}/admin/util/load-balance-test" target="_blank">Click to display load balancing information page</a>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="portlet box grey">
				<div class="portlet-title">
					<div class="caption">
						<i class="fa fa-reorder"></i> Druid data source monitoring
					</div>
					<div class="tools">
						<a class="collapse" href="javascript:;"></a>
					</div>
				</div>
				<div class="portlet-body">
					<div class="note note-info">
						<P> This project uses developed by Ali maintained Druid database connection pool. Druid can provide powerful monitoring and extensions. Druid built a StatViewServlet provide statistics to show for the Druid . </ P>					</div>
					<a class="btn blue" href="${ctx}/druid/" target="_blank">Click here to visit Druid data source monitoring page</a>
				</div>
			</div>
		</div>
	</div>

	<div class="row">
		<div class="col-md-6">
			<div class="portlet box grey">
				<div class="portlet-title">
					<div class="caption">
						<i class="fa fa-reorder"></i> Temporarily set the system time
					</div>
					<div class="tools">
						<a class="collapse" href="javascript:;"></a>
					</div>
				</div>
				<div class="portlet-body">
					<form class="form-horizontal form-bordered form-label-stripped form-validation"
						action='${ctx}/admin/util/systime/setup' method="post" data-editrulesurl="false">
						<div class="form-body" style="min-height: 250px">
							<div class="note note-info">
								<P> This function is primarily used in the development and testing phase, the temporary " tampering " Adjusting the current time information system to simulate the operation of the system after a period of time , and then perform regular tasks and other operations . </ P>
<P> adjust the system time can cause the entire system has been fixed at a set time , we will not continue to advance forward . Therefore, the temporary operation is completed in time " to restore the system time " to restore the real-time current system time . </ P>
<P> To avoid forgetting to perform manual recovery operation , in the " temporary adjust the system time" action , by default after 10 minutes forced back to the current system time. </ P>
							</div>
							<div class="form-group">
								<label class="control-label">Specifies a temporary system time</label>
								<div class="controls">
									<input type="text" name="time" class="form-control" data-picker="date-time" required="true">
								</div>
							</div>
						</div>
						<div class="form-actions right">
							<button class="btn blue" type="submit">
								<i class="fa fa-check"></i> Temporary adjust the system time
							</button>
							<button class="btn blue btn-post-url" type="button" data-url="${ctx}/admin/util/systime/reset"
								data-confirm="false">Recovery System Time</button>
						</div>
					</form>
				</div>
			</div>
		</div>
		<div class="col-md-6">
			<div class="portlet box grey">
				<div class="portlet-title">
					<div class="caption">
						<i class="fa fa-reorder"></i> Message Service listener control
					</div>
					<div class="tools">
						<a class="collapse" href="javascript:;"></a>
					</div>
				</div>
				<div class="portlet-body">
					<form class="form-horizontal form-bordered form-label-stripped form-validation"
						action='${ctx}/admin/util/systime/setup' method="post" data-editrulesurl="false">
						<div class="form-body" style="min-height: 250px">
							<div class="note note-info">
								<P> In the development dev_mode = true mode of operation , in order to avoid competing with each other to receive a message , the message listener service is disabled by default . </ P>
<P> In the development and testing process, according to need manual control to open or close monitoring services . </ P>
							</div>
						</div>
						<div class="form-actions right">
							<button class="btn blue btn-post-url" type="button"
								data-url="${ctx}/admin/util/brokered-message/listener-state?state=startup" data-confirm="false">Start listening service</button>
							<button class="btn blue btn-post-url" type="button"
								data-url="${ctx}/admin/util/brokered-message/listener-state?state=shutdown" data-confirm="false">
Close monitoring services</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
</body>
</html>