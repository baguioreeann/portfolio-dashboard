package com.reeann.portfoliodashboard.service;

import java.util.List;

public record RefreshResult(int updated, List<String> errors) {
}
