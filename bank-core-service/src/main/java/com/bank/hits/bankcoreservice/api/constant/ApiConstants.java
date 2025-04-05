package com.bank.hits.bankcoreservice.api.constant;

public class ApiConstants {

    // Версия API
    public static final String API_VERSION = "/core";

    // Базовые пути контроллеров
    public static final String ACCOUNTS_BASE = API_VERSION + "/bank_account";
    public static final String EMPLOYEES_BASE = API_VERSION + "/employee";

    // Эндпоинты для работы со счетами
    // для клиентов
    public static final String CREATE_ACCOUNT = "/create"; // POST /accounts/create
    public static final String CLOSE_ACCOUNT = "/close"; // POST /accounts/{accountId}/close

    // для сотрудников
    public static final String BLOCK_CLIENT_ACCOUNTS = "/{clientId}/block"; // POST /accounts/{clientId}/block
    public static final String UNBLOCK_CLIENT_ACCOUNTS = "/{clientId}/unblock"; // POST /accounts/{clientId}/unblock
    public static final String CLIENT_INFO = "/bank_account/{userId}/list"; // GET /accounts/{clientId}/info

    // Эндпоинты для работы с транзакциями
    public static final String TOP_UP = "/{accountId}/top_up"; // POST /accounts/deposit
    public static final String WITHDRAW = "/{accountId}/withdraw"; // POST /accounts/withdraw
    public static final String ACCOUNT_HISTORY = "/operation_history"; // GET /accounts/{accountId}/history



    public static final String GET_ACCOUNT = "/account/{accountId}";
    public static final String GET_ACCOUNTS = "/list";
    public static final String CREATE_CLIENT = "/client/create";

    public static final String TRANSFER = "/transfer";
    public static final String TRANSFER_INFO = "/transferInfo";
    public static final String LOAN_PAYMENTS = "/loan/{loanId}/payments";
}