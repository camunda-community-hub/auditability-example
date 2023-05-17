package io.camunda.zeebe;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.camunda.zeebe.exporter.api.Exporter;
import io.camunda.zeebe.exporter.api.context.Context;
import io.camunda.zeebe.exporter.api.context.Controller;
import io.camunda.zeebe.protocol.record.RecordType;
import io.camunda.zeebe.protocol.record.ValueType;
import io.camunda.zeebe.protocol.record.value.BpmnElementType;
import io.camunda.zeebe.protocol.record.value.ProcessInstanceRecordValue;

public class AuditExporter implements Exporter {
  public static final Logger LOGGER = LoggerFactory.getLogger("io.camunda.zeebe");
    private Controller controller;
    @Override
    public void configure(Context context) {

    }
    @Override
    public void open(Controller controller) {
        this.controller = controller;
    }
    @Override
    public void close() {

    }
    @Override
    public void export(io.camunda.zeebe.protocol.record.Record<?> record) {
      if (record.getRecordType() == RecordType.EVENT) {
        if(record.getValueType() == ValueType.PROCESS_INSTANCE) {
            ProcessInstanceRecordValue value = (ProcessInstanceRecordValue) record.getValue();
            //ProcessInstanceIntent intent = (ProcessInstanceIntent) record.getIntent();
            
            if (value.getBpmnElementType().equals(BpmnElementType.END_EVENT)) {
              pingAuditApp(value.getProcessInstanceKey());
            }
    

        }
      }
      this.controller.updateLastExportedRecordPosition(record.getPosition());
    }
    
    public void pingAuditApp(Long processInstanceKey) {
        HttpPost httpPost = new HttpPost("http://audit:8090/api/audit");
        httpPost.addHeader("Content-Type", "application/json");

        String data = "{\"processInstanceKey\":\"" + processInstanceKey + "\"}";
        httpPost.setEntity(new StringEntity(data));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                
            }
        } catch(IOException e) {
          LOGGER.error("Error reaching the audit app", e);
        }
    }

}
