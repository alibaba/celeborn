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
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * CreateSchema
 */
@JsonPropertyOrder({
  CreateSchema.JSON_PROPERTY_NAME,
  CreateSchema.JSON_PROPERTY_CATALOG_NAME,
  CreateSchema.JSON_PROPERTY_COMMENT,
  CreateSchema.JSON_PROPERTY_PROPERTIES
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.6.0")
public class CreateSchema {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_CATALOG_NAME = "catalog_name";
  private String catalogName;

  public static final String JSON_PROPERTY_COMMENT = "comment";
  private String comment;

  public static final String JSON_PROPERTY_PROPERTIES = "properties";
  private Map<String, String> properties = new HashMap<>();

  public CreateSchema() { 
  }

  public CreateSchema name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of schema, relative to parent catalog.
   * @return name
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getName() {
    return name;
  }


  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setName(String name) {
    this.name = name;
  }


  public CreateSchema catalogName(String catalogName) {
    this.catalogName = catalogName;
    return this;
  }

   /**
   * Name of parent catalog.
   * @return catalogName
  **/
  @javax.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CATALOG_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getCatalogName() {
    return catalogName;
  }


  @JsonProperty(JSON_PROPERTY_CATALOG_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }


  public CreateSchema comment(String comment) {
    this.comment = comment;
    return this;
  }

   /**
   * User-provided free-form text description.
   * @return comment
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_COMMENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getComment() {
    return comment;
  }


  @JsonProperty(JSON_PROPERTY_COMMENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setComment(String comment) {
    this.comment = comment;
  }


  public CreateSchema properties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public CreateSchema putPropertiesItem(String key, String propertiesItem) {
    if (this.properties == null) {
      this.properties = new HashMap<>();
    }
    this.properties.put(key, propertiesItem);
    return this;
  }

   /**
   * A map of key-value properties attached to the securable.
   * @return properties
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PROPERTIES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Map<String, String> getProperties() {
    return properties;
  }


  @JsonProperty(JSON_PROPERTY_PROPERTIES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }


  /**
   * Return true if this CreateSchema object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateSchema createSchema = (CreateSchema) o;
    return Objects.equals(this.name, createSchema.name) &&
        Objects.equals(this.catalogName, createSchema.catalogName) &&
        Objects.equals(this.comment, createSchema.comment) &&
        Objects.equals(this.properties, createSchema.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, catalogName, comment, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateSchema {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    catalogName: ").append(toIndentedString(catalogName)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

    // add `name` to the URL query string
    if (getName() != null) {
      joiner.add(String.format("%sname%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `catalog_name` to the URL query string
    if (getCatalogName() != null) {
      joiner.add(String.format("%scatalog_name%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getCatalogName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `comment` to the URL query string
    if (getComment() != null) {
      joiner.add(String.format("%scomment%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getComment()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `properties` to the URL query string
    if (getProperties() != null) {
      for (String _key : getProperties().keySet()) {
        joiner.add(String.format("%sproperties%s%s=%s", prefix, suffix,
            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, _key, containerSuffix),
            getProperties().get(_key), URLEncoder.encode(String.valueOf(getProperties().get(_key)), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
      }
    }

    return joiner.toString();
  }
}

