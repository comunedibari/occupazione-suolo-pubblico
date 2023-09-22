package it.fincons.osp.dto;

import lombok.*;

@Data
public class DashboardDTO {
    private String dashboard;

    public DashboardDTO(String dashboard) {
        this.dashboard = dashboard;
    }
}
