package cn.fishmaple.logsight.thread;

import cn.fishmaple.logsight.dao.consts.LogFileStatus;
import cn.fishmaple.logsight.dao.dto.LogFieldDTO;
import cn.fishmaple.logsight.dao.dto.LogFieldFileDTO;
import cn.fishmaple.logsight.dao.mapper.LogFieldFileMapper;
import cn.fishmaple.logsight.dao.mapper.LogFieldMapper;
import cn.fishmaple.logsight.handler.FileScanHandler;
import cn.fishmaple.logsight.service.ApplicationContextProvider;
import cn.fishmaple.logsight.util.TimeUtil;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@DependsOn(value = {"logFieldMapper","logFieldFileMapper","fileScanHandler"})
public class FileLoadThread extends Thread{
    LogFieldMapper logFieldMapper;
    LogFieldFileMapper logFieldFileMapper;
    FileScanHandler fileScanHandler;
    Integer scanTimes;
    @PostConstruct
    public void init(){
       new FileLoadThread().start();
    }

    private void initThread(){
        scanTimes = 0;
        while (null==logFieldMapper||null==fileScanHandler||null==logFieldFileMapper){
            logFieldMapper = ApplicationContextProvider.getBean(LogFieldMapper.class);
            logFieldFileMapper = ApplicationContextProvider.getBean(LogFieldFileMapper.class);
            fileScanHandler = ApplicationContextProvider.getBean(FileScanHandler.class);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    public void run(){
        initThread();
        while (true) {
            boolean overallScan = (scanTimes>>2)>0;
            List<LogFieldDTO> list;
            if(overallScan){
                list = logFieldMapper.selectUnClosedField();
                scanTimes = 0;
            }else{
                list = logFieldMapper.selectUnScannedField();
            }
            for (LogFieldDTO logFieldDTO : list) {
                int count=0;
                Collection<String> files = fileScanHandler.scanFile(logFieldDTO.getPath());
                Set<LogFieldFileDTO> nowaFiles = logFieldFileMapper.getFilesByFieldId(logFieldDTO.getId());
                if(overallScan){
                    logFieldDTO.setFileCount(null);
                    logFieldDTO.setTimeline(new Date());
                    logFieldDTO.setStatus(0);
                    logFieldMapper.scanUpdate(logFieldDTO);
                }
                double size = 0L;
                for (LogFieldFileDTO logFieldFileDTO : nowaFiles) {
                    File file = new File(logFieldFileDTO.getPathName());
                    if(file.isFile()&&!file.isHidden()){
                        files.remove(logFieldFileDTO.getPathName());
                        logFieldFileMapper.addOneFile(new LogFieldFileDTO(logFieldDTO.getId(), new Date(),
                                logFieldFileDTO.getPathName(),TimeUtil.getEarlyHour(-1,0),logFieldFileDTO.getFileSize(), LogFileStatus.NORMAL));
                        count++;
                        size += ((double)file.length())/1024/1024;
                    }else{
                        logFieldFileMapper.addOneFile(new LogFieldFileDTO(logFieldDTO.getId(), new Date(),
                                logFieldFileDTO.getPathName(),TimeUtil.getEarlyHour(-1,0),0L,LogFileStatus.DELETED));
                    }
                }
                for (String logFile : files) {
                    File file = new File(logFile);
                    if(file.isFile()&&!file.isHidden()){
                        logFieldFileMapper.addOneFile(new LogFieldFileDTO(logFieldDTO.getId(), new Date(),
                                logFile,TimeUtil.getEarlyHour(-1,0),0L,LogFileStatus.NORMAL));
                        count++;
                        size += ((double)file.length())/1024/1024;
                    }
                }
                if(count<1){
                    logFieldDTO.setFileCount(0);
                    logFieldDTO.setTimeline(new Date());
                    logFieldDTO.setStatus(3);
                }else{
                    logFieldDTO.setFileCount(count);
                    logFieldDTO.setTimeline(new Date());
                    logFieldDTO.setStatus(1);
                    logFieldDTO.setTotalSize(size);
                }
                logFieldMapper.scanUpdate(logFieldDTO);
            }
            scanTimes++;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]){
        for(int i =0;i<2000;i++){
            System.out.println((i>>10)+"*****"+i);
        }

    }
}
