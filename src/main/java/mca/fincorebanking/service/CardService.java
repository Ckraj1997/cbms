package mca.fincorebanking.service;

import java.util.List;

import mca.fincorebanking.entity.ChequeBookRequest;
import mca.fincorebanking.entity.DebitCard;

public interface CardService {

    DebitCard getCardByAccountId(Long accountId);

    DebitCard issueCard(Long accountId);

    void toggleCardStatus(Long cardId);

    ChequeBookRequest requestChequeBook(Long accountId, String type);

    List<ChequeBookRequest> getChequeRequestsByAccount(Long accountId);

    List<ChequeBookRequest> getAllPendingChequeRequests();

    List<ChequeBookRequest> getChequeRequestsByStatus(String pending);

    void updateChequeRequestStatus(Long id, String pending_admin);
}