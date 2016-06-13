package lab.s2jh.module.bpm.web;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lab.s2jh.core.exception.WebException;
import lab.s2jh.module.bpm.service.ActivitiService;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/admin/bpm")
public class ActivitiController {

    protected static Logger logger = LoggerFactory.getLogger(ActivitiController.class);

    @Autowired
    protected ActivitiService activitiService;

    /**
     * Flow diagram shows the response
     * Loose access control flowchart employed herein , if the business need to restrict access to the flowchart need to add the appropriate control logic
     */
    @RequestMapping(value = "/diagram", method = RequestMethod.GET)
    @ResponseBody
    public void processInstanceImage(HttpServletRequest request, HttpServletResponse response) {
        InputStream imageStream = null;
        String bizKey = request.getParameter("bizKey");
        if (StringUtils.isNotBlank(bizKey)) {
            imageStream = activitiService.buildProcessImageByBizKey(bizKey);
        } else {
            String processInstanceId = request.getParameter("processInstanceId");
            imageStream = activitiService.buildProcessImageByProcessInstanceId(processInstanceId);
        }

        if (imageStream == null) {
            return;
        }

     // Output corresponding to the content of the resource objects
        byte[] b = new byte[1024];
        int len = -1;
        ServletOutputStream out;
        try {
            out = response.getOutputStream();
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                out.write(b, 0, len);
            }
            imageStream.close();
            out.close();
        } catch (IOException e) {
            logger.error("Output process image error", e);
            throw new WebException("Exception processing flow diagram", e);
        }
    }
}
