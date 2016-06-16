<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
	<form:form class="form-horizontal form-bordered form-label-stripped form-validation control-label-lg"
		action="${ctx}/admin/crawl/crawl-data/crawler/startup" modelAttribute="crawlConfig" method="post"
		data-editrulesurl="false" id="crawlForm">
		<div class="form-actions">
			<button class="btn blue" type="submit">
				<i class="fa fa-check"></i> Start collecting Crawls
			</button>
			<button type="button" class="btn default btn-post-url" data-confirm="Confirm forced stop crawl run ?"
				data-url="${ctx}/admin/crawl/crawl-data/crawler/shutdown">Forced to stop running crawls</button>
		</div>
		<div class="form-body">
			<div class="row">
				<div class="col-md-6">
					<div class="form-group">
						<label class="control-label">Force Recrawl page content</label>
						<div class="controls controls-radiobuttons">
							<form:radiobuttons path="forceRefetch" items="${applicationScope.cons.booleanLabelMap}" class="form-control" />
						</div>
					</div>
					<div class="form-group">
						<label class="control-label">Forced re- parsing page content</label>
						<div class="controls controls-radiobuttons">
							<form:radiobuttons path="forceReparse" items="${applicationScope.cons.booleanLabelMap}" class="form-control" />
						</div>
					</div>
				</div>
				<div class="col-md-6">
					<div class="form-group">
						<label class="control-label">Concurrent crawls threads</label>
						<div class="controls">
							<form:input path="threadNum" class="form-control" required="true" data-rule-min="1" data-rule-max="100" />
							<span class="help-block">For fast, no anti- reptile sites can be set according to a larger machine performance ; anyway, set to a smaller number of</span>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label">Crawl Access minimum interval ( in seconds )</label>
						<div class="controls">
							<form:input path="fetchMinInterval" class="form-control" required="true" data-rule-max="300" />
							<span class="help-block">Some sites do a certain anti- crawler control , such as restrictions on user request interval may not be too fast , through a reasonable set this parameter to circumvent the blockade site</span>
						</div>
					</div>
				</div>
			</div>

			<div class="row" data-equal-height="false">
				<div class="col-md-12">
					<div class="form-group">
						<label class="control-label">Seeds URL</label>
						<div class="controls">
							<textarea name="urls" rows="6" class="form-control" />
							<span class="help-block">Please fill meet the following URL list of regular expressions , one per line URL ; blank for all 200 non- success status page Recrawl resolve</span>
						</div>
					</div>
					<div class="form-group">
						<label class="control-label">Valid URL regular list</label>
						<div class="controls">
							<c:forEach items="${crawlParseFilters}" var="item">
								<c:if test="${item.urlFilterRegex!=null}">
									<p class="form-control-static" title="${item.getClass().name}">${item.urlFilterRegex}</p>
								</c:if>
							</c:forEach>
						</div>
					</div>
				</div>
			</div>
		</div>
	</form:form>

	<script type="text/javascript">
        $("#crawlForm").on("form-submit-success", function() {
            var $this = $(this);
            $this.closest(".tabbable").find(">.nav .tab-crawl-logger").click();
        })
    </script>
</body>
</html>