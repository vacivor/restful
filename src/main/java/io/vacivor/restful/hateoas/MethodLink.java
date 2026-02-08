package io.vacivor.restful.hateoas;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;

public class MethodLink extends Link {
  private final String method;

  public MethodLink(String href, LinkRelation rel, String method) {
    super(href, rel);
    this.method = method;
  }

  public String getMethod() {
    return method;
  }

  public static MethodLink of(String href, String rel, String method) {
    return new MethodLink(href, LinkRelation.of(rel), method);
  }
}
