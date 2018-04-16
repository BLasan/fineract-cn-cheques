/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.mifos.cheque.service.rest;

import io.mifos.cheque.api.v1.PermittableGroupIds;
import io.mifos.cheque.api.v1.domain.Cheque;
import io.mifos.cheque.api.v1.domain.ChequeTransaction;
import io.mifos.cheque.service.ServiceConstants;
import io.mifos.cheque.service.internal.command.ChequeTransactionCommand;
import io.mifos.cheque.service.internal.format.MICRParser;
import io.mifos.cheque.service.internal.service.ChequeService;
import javax.validation.Valid;
import org.apache.fineract.cn.anubis.annotation.AcceptedTokenType;
import org.apache.fineract.cn.anubis.annotation.Permittable;
import org.apache.fineract.cn.command.gateway.CommandGateway;
import org.apache.fineract.cn.lang.ServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class ChequeTransactionRestController {

  private final Logger logger;
  private final CommandGateway commandGateway;
  private final ChequeService chequeService;

  @Autowired
  public ChequeTransactionRestController(@Qualifier(ServiceConstants.LOGGER_NAME) final Logger logger,
                                         final CommandGateway commandGateway,
                                         final ChequeService chequeService) {
    super();
    this.logger = logger;
    this.commandGateway = commandGateway;
    this.chequeService = chequeService;
  }

  @Permittable(value = AcceptedTokenType.TENANT, groupId = PermittableGroupIds.CHEQUE_TRANSACTION)
  @RequestMapping(
      value = "/",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE}
  )
  @ResponseBody
  ResponseEntity<Void> process(@RequestBody @Valid final ChequeTransaction chequeTransaction) {

    final Cheque cheque = chequeTransaction.getCheque();

    if (this.chequeService.chequeExists(chequeTransaction.getCheque())) {
      throw ServiceException.conflict("Cheque {0} already used.",
          MICRParser.toIdentifier(chequeTransaction.getCheque().getMicr()));
    }

    this.commandGateway.process(new ChequeTransactionCommand(cheque, chequeTransaction.getChequesReceivableAccount(),  chequeTransaction.getCreditorAccountNumber()));

    return ResponseEntity.accepted().build();
  }
}
