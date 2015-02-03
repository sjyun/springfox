package com.mangofactory.documentation.builders;

import com.mangofactory.documentation.service.TokenRequestEndpoint;
import com.mangofactory.documentation.service.AuthorizationCodeGrant;
import com.mangofactory.documentation.service.TokenEndpoint;

import static com.mangofactory.documentation.builders.BuilderDefaults.*;

public class AuthorizationCodeGrantBuilder {
  private TokenRequestEndpoint tokenRequestEndpoint;
  private TokenEndpoint tokenEndpoint;

  public AuthorizationCodeGrantBuilder tokenRequestEndpoint(TokenRequestEndpoint tokenRequestEndpoint) {
    this.tokenRequestEndpoint = defaultIfAbsent(tokenRequestEndpoint, this.tokenRequestEndpoint);
    return this;
  }

  public AuthorizationCodeGrantBuilder tokenEndpoint(TokenEndpoint tokenEndpoint) {
    this.tokenEndpoint = defaultIfAbsent(tokenEndpoint, this.tokenEndpoint);
    return this;
  }

  public AuthorizationCodeGrant build() {
    return new AuthorizationCodeGrant(tokenRequestEndpoint, tokenEndpoint);
  }
}