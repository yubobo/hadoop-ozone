/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.ozone.om.request.key.acl;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import org.apache.hadoop.ozone.OzoneAcl;
import org.apache.hadoop.ozone.om.helpers.OmKeyInfo;
import org.apache.hadoop.ozone.om.helpers.OzoneAclUtil;
import org.apache.hadoop.ozone.om.request.util.OmResponseUtil;
import org.apache.hadoop.ozone.om.response.key.acl.OMKeyAclResponse;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.hadoop.ozone.om.response.OMClientResponse;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMRequest;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.OMResponse;
import org.apache.hadoop.ozone.protocol.proto.OzoneManagerProtocolProtos.SetAclResponse;

/**
 * Handle add Acl request for bucket.
 */
public class OMKeySetAclRequest extends OMKeyAclRequest {

  private static final Logger LOG =
      LoggerFactory.getLogger(OMKeySetAclRequest.class);

  private String path;
  private List<OzoneAcl> ozoneAcls;

  public OMKeySetAclRequest(OMRequest omRequest) {
    super(omRequest);
    OzoneManagerProtocolProtos.SetAclRequest setAclRequest =
        getOmRequest().getSetAclRequest();
    path = setAclRequest.getObj().getPath();
    ozoneAcls = Lists.newArrayList(
        OzoneAclUtil.fromProtobuf(setAclRequest.getAclList()));
  }

  @Override
  String getPath() {
    return path;
  }

  @Override
  OMResponse.Builder onInit() {
    return OmResponseUtil.getOMResponseBuilder(getOmRequest());
  }

  @Override
  OMClientResponse onSuccess(OMResponse.Builder omResponse,
      OmKeyInfo omKeyInfo, boolean operationResult) {
    omResponse.setSuccess(operationResult);
    omResponse.setSetAclResponse(SetAclResponse.newBuilder()
        .setResponse(operationResult));
    return new OMKeyAclResponse(omResponse.build(), omKeyInfo);
  }

  @Override
  void onComplete(Result result, boolean operationResult,
      IOException exception, long trxnLogIndex) {
    switch (result) {
    case SUCCESS:
      if (LOG.isDebugEnabled()) {
        LOG.debug("Set acl: {} to path: {} success!", ozoneAcls, path);
      }
      break;
    case FAILURE:
      LOG.error("Set acl {} to path {} failed!", ozoneAcls, path, exception);
      break;
    default:
      LOG.error("Unrecognized Result for OMKeySetAclRequest: {}",
          getOmRequest());
    }
  }

  @Override
  boolean apply(OmKeyInfo omKeyInfo, long trxnLogIndex) {
    // No need to check not null here, this will be never called with null.
    return omKeyInfo.setAcls(ozoneAcls);
  }
}

