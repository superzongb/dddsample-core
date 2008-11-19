package se.citerus.dddsample.application.impl;

import org.springframework.transaction.annotation.Transactional;
import se.citerus.dddsample.application.HandlingEventService;
import se.citerus.dddsample.application.messaging.HandlingEventRegistrationAttempt;
import se.citerus.dddsample.domain.model.handling.CannotCreateHandlingEventException;
import se.citerus.dddsample.domain.model.handling.HandlingEvent;
import se.citerus.dddsample.domain.model.handling.HandlingEventFactory;
import se.citerus.dddsample.domain.model.handling.HandlingEventRepository;
import se.citerus.dddsample.domain.service.DomainEventNotifier;

public final class HandlingEventServiceImpl implements HandlingEventService {

  private final HandlingEventRepository handlingEventRepository;
  private final DomainEventNotifier domainEventNotifier;
  private final HandlingEventFactory handlingEventFactory;

  public HandlingEventServiceImpl(HandlingEventRepository handlingEventRepository, DomainEventNotifier domainEventNotifier, HandlingEventFactory handlingEventFactory) {
    this.handlingEventRepository = handlingEventRepository;
    this.domainEventNotifier = domainEventNotifier;
    this.handlingEventFactory = handlingEventFactory;
  }

  /*
   NOTE:
     The cargo instance that's loaded and associated with the handling event is
     in an inconsitent state, because the cargo delivery history's collection of
     events does not contain the event created here. However, this is not a problem,
     because cargo is in a different aggregate from handling event.

     The rules of an aggregate dictate that all consistency rules within the aggregate
     are enforced synchronously in the transaction, but consistency rules of other aggregates
     are enforced by asynchronous updates, after the commit of this transaction.
  */
  @Override
  @Transactional(readOnly = false, rollbackFor = {CannotCreateHandlingEventException.class})
  public void register(HandlingEventRegistrationAttempt attempt) throws CannotCreateHandlingEventException {
    final HandlingEvent event = handlingEventFactory.createHandlingEvent(attempt.getDate(), attempt.getTrackingId(), attempt.getVoyageNumber(), attempt.getUnLocode(), attempt.getType());
    handlingEventRepository.save(event);
    domainEventNotifier.cargoWasHandled(event);
  }

}