package ru.ik.deploy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeployFileGenerator {

    private String[] patchList;
    private boolean skipUsePatchPath;
    private boolean needAdcgnjar;
    private boolean needOacoreRestart;
    private String cloneList;
    private int singlePatchInstallTimeout;
    private String emailCC;
    private String genuineCommiter;
    private String deployFileName;
    private String deployFileContent;
    private boolean needAdutlrcmp;
    private boolean needWfStop;
    private boolean needWfStart;
    private boolean needWfRestart;

    public void setPatchList(String[] patchList) {
        this.patchList = patchList;
    }

    public void setSkipUsePatchPath(boolean skipUsePatchPath) {
        this.skipUsePatchPath = skipUsePatchPath;
    }

    public void setNeedAdcgnjar(boolean needAdcgnjar) {
        this.needAdcgnjar = needAdcgnjar;
    }

    public void setNeedOacoreRestart(boolean needOacoreRestart) {
        this.needOacoreRestart = needOacoreRestart;
    }

    public void setCloneList(String cloneList) {
        this.cloneList = cloneList;
    }

    public void setSinglePatchInstallTimeout(int singlePatchInstallTimeout) {
        this.singlePatchInstallTimeout = singlePatchInstallTimeout;
    }

    public void setEmailCC(String emailCC) {
        this.emailCC = emailCC;
    }

    public void setGenuineCommiter(String genuineCommiter) {
        this.genuineCommiter = genuineCommiter;
    }

    public String getDeployFileName() {
        return deployFileName;
    }

    public String getDeployFileContent() {
        return deployFileContent;
    }

    public void generate() {
        String deployFileNameText = "task_" + getOEBSList(patchList) + "_" + getCurrentDateFormatted();
        deployFileName = deployFileNameText.length() > 255 ? deployFileNameText.substring(0, 255) : deployFileNameText;

        StringBuilder sb = new StringBuilder("[patch_deploy]\n");
        boolean usePatchPath = getUsePatchPath(patchList);
        if (usePatchPath) {
            sb.append("use_patch_path = True\n");
        }
        sb.append("installorder = ").append(getPatchList(patchList));
        StringBuilder installOrder = new StringBuilder();
        if (needWfStop) {
            installOrder.append(",wf-stop");
        }
        if (needAdcgnjar) {
            installOrder.append(",adcgnjar");
        }
        if (needOacoreRestart) {
            installOrder.append(",oacorereload");
        }
        if (needAdutlrcmp) {
            installOrder.append(",adutlrcmp");
        }
        if (needWfRestart) {
            installOrder.append(",wf-restart");
        }
        if (needWfStart) {
            installOrder.append(",wf-start");
        }
        
        if (installOrder.length() > 0 && installOrder.charAt(0) == ',') {
            sb.append(installOrder.substring(1));
        } else {
            sb.append(installOrder);
        }
        
        sb.append("\n");
        String cloneListStr = cloneList;
        sb.append("installto = ").append(cloneListStr).append("\n");
        sb.append("single_patch_install_timeout = ").append(singlePatchInstallTimeout == 0 ? 1000 : singlePatchInstallTimeout).append("\n");
        sb.append("patch_deploy_description = '").append(deployFileNameText).append(" ").append(cloneListStr).append("'\n");
        sb.append("email_cc = ").append(emailCC).append("\n");
        sb.append("genuine_commiter = ").append(genuineCommiter).append("\n");
        
        deployFileContent = sb.toString();
    }

    private String getOEBSList(String[] patchListArray) {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile(".*(OEBS-\\d+).*");

        for (String patch : patchListArray) {
            Matcher m = p.matcher(patch);
            if (m.matches()) {
                if (sb.length() > 0) {
                    sb.append("_");
                }
                sb.append(m.group(1));
            }
        }

        return sb.toString();
    }    

    private String getCurrentDateFormatted() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy_HHmmss");
        return dateFormat.format(new Date());
    }  
    
    private boolean getUsePatchPath(String[] patches) {
        if (skipUsePatchPath) {
            return false;
        }
        for (String patch : patches) {
            if (patch.contains("PRODUCT")) {
                return true;
            }
        }

        return false;
    }   
    
    private String getPatchList(String[] patches) {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile(".*((HOTFIX[^\\/]*|PRODUCT[^\\/]*)/.*sh).*");
        for (String patch : patches) {
            Matcher m = p.matcher(patch.replace('\\', '/'));
            if (m.matches()) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append("trunk/" + m.group(1));
            }
        }
        return sb.toString();
    }

    public void setNeedAdutlrcmp(boolean needAdutlrcmp) {
        this.needAdutlrcmp = needAdutlrcmp;
    }

    public void setNeedWfStop(boolean needWfStop) {
        this.needWfStop = needWfStop;
    }

    public void setNeedWfStart(boolean needWfStart) {
        this.needWfStart = needWfStart;
    }

    public void setNeedWfRestart(boolean needWfRestart) {
        this.needWfRestart = needWfRestart;
    }

    public boolean isNeedAdutlrcmp() {
        return needAdutlrcmp;
    }

    public boolean isNeedWfStop() {
        return needWfStop;
    }

    public boolean isNeedWfStart() {
        return needWfStart;
    }

    public boolean isNeedWfRestart() {
        return needWfRestart;
    }
    
}
