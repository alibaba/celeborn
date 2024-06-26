/*
 * Unity Catalog API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 0.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.apache.celeborn.client.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import org.apache.celeborn.client.model.CreateFunction;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * CreateFunctionRequest
 */
@JsonPropertyOrder({
  CreateFunctionRequest.JSON_PROPERTY_FUNCTION_INFO
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.6.0")
public class CreateFunctionRequest {
  public static final String JSON_PROPERTY_FUNCTION_INFO = "function_info";
  private CreateFunction functionInfo;

  public CreateFunctionRequest() { 
  }

  public CreateFunctionRequest functionInfo(CreateFunction functionInfo) {
    this.functionInfo = functionInfo;
    return this;
  }

   /**
   * Get functionInfo
   * @return functionInfo
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_FUNCTION_INFO)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public CreateFunction getFunctionInfo() {
    return functionInfo;
  }


  @JsonProperty(JSON_PROPERTY_FUNCTION_INFO)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setFunctionInfo(CreateFunction functionInfo) {
    this.functionInfo = functionInfo;
  }


  /**
   * Return true if this CreateFunctionRequest object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateFunctionRequest createFunctionRequest = (CreateFunctionRequest) o;
    return Objects.equals(this.functionInfo, createFunctionRequest.functionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(functionInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateFunctionRequest {\n");
    sb.append("    functionInfo: ").append(toIndentedString(functionInfo)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  /**
   * Convert the instance into URL query string.
   *
   * @return URL query string
   */
  public String toUrlQueryString() {
    return toUrlQueryString(null);
  }

  /**
   * Convert the instance into URL query string.
   *
   * @param prefix prefix of the query string
   * @return URL query string
   */
  public String toUrlQueryString(String prefix) {
    String suffix = "";
    String containerSuffix = "";
    String containerPrefix = "";
    if (prefix == null) {
      // style=form, explode=true, e.g. /pet?name=cat&type=manx
      prefix = "";
    } else {
      // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
      prefix = prefix + "[";
      suffix = "]";
      containerSuffix = "]";
      containerPrefix = "[";
    }

    StringJoiner joiner = new StringJoiner("&");

    // add `function_info` to the URL query string
    if (getFunctionInfo() != null) {
      joiner.add(getFunctionInfo().toUrlQueryString(prefix + "function_info" + suffix));
    }

    return joiner.toString();
  }
}

