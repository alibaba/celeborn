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
import org.apache.celeborn.client.model.ColumnTypeName;
import org.apache.celeborn.client.model.DependencyList;
import org.apache.celeborn.client.model.FunctionParameterInfos;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * FunctionInfo
 */
@JsonPropertyOrder({
  FunctionInfo.JSON_PROPERTY_NAME,
  FunctionInfo.JSON_PROPERTY_CATALOG_NAME,
  FunctionInfo.JSON_PROPERTY_SCHEMA_NAME,
  FunctionInfo.JSON_PROPERTY_INPUT_PARAMS,
  FunctionInfo.JSON_PROPERTY_DATA_TYPE,
  FunctionInfo.JSON_PROPERTY_FULL_DATA_TYPE,
  FunctionInfo.JSON_PROPERTY_RETURN_PARAMS,
  FunctionInfo.JSON_PROPERTY_ROUTINE_BODY,
  FunctionInfo.JSON_PROPERTY_ROUTINE_DEFINITION,
  FunctionInfo.JSON_PROPERTY_ROUTINE_DEPENDENCIES,
  FunctionInfo.JSON_PROPERTY_PARAMETER_STYLE,
  FunctionInfo.JSON_PROPERTY_IS_DETERMINISTIC,
  FunctionInfo.JSON_PROPERTY_SQL_DATA_ACCESS,
  FunctionInfo.JSON_PROPERTY_IS_NULL_CALL,
  FunctionInfo.JSON_PROPERTY_SECURITY_TYPE,
  FunctionInfo.JSON_PROPERTY_SPECIFIC_NAME,
  FunctionInfo.JSON_PROPERTY_COMMENT,
  FunctionInfo.JSON_PROPERTY_PROPERTIES,
  FunctionInfo.JSON_PROPERTY_FULL_NAME,
  FunctionInfo.JSON_PROPERTY_CREATED_AT,
  FunctionInfo.JSON_PROPERTY_UPDATED_AT,
  FunctionInfo.JSON_PROPERTY_FUNCTION_ID,
  FunctionInfo.JSON_PROPERTY_EXTERNAL_LANGUAGE
})
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.6.0")
public class FunctionInfo {
  public static final String JSON_PROPERTY_NAME = "name";
  private String name;

  public static final String JSON_PROPERTY_CATALOG_NAME = "catalog_name";
  private String catalogName;

  public static final String JSON_PROPERTY_SCHEMA_NAME = "schema_name";
  private String schemaName;

  public static final String JSON_PROPERTY_INPUT_PARAMS = "input_params";
  private FunctionParameterInfos inputParams;

  public static final String JSON_PROPERTY_DATA_TYPE = "data_type";
  private ColumnTypeName dataType;

  public static final String JSON_PROPERTY_FULL_DATA_TYPE = "full_data_type";
  private String fullDataType;

  public static final String JSON_PROPERTY_RETURN_PARAMS = "return_params";
  private FunctionParameterInfos returnParams;

  /**
   * Function language. When **EXTERNAL** is used, the language of the routine function should be specified in the __external_language__ field,  and the __return_params__ of the function cannot be used (as **TABLE** return type is not supported), and the __sql_data_access__ field must be **NO_SQL**. 
   */
  public enum RoutineBodyEnum {
    SQL("SQL"),
    
    EXTERNAL("EXTERNAL");

    private String value;

    RoutineBodyEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static RoutineBodyEnum fromValue(String value) {
      for (RoutineBodyEnum b : RoutineBodyEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_ROUTINE_BODY = "routine_body";
  private RoutineBodyEnum routineBody;

  public static final String JSON_PROPERTY_ROUTINE_DEFINITION = "routine_definition";
  private String routineDefinition;

  public static final String JSON_PROPERTY_ROUTINE_DEPENDENCIES = "routine_dependencies";
  private DependencyList routineDependencies;

  /**
   * Function parameter style. **S** is the value for SQL.
   */
  public enum ParameterStyleEnum {
    S("S");

    private String value;

    ParameterStyleEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ParameterStyleEnum fromValue(String value) {
      for (ParameterStyleEnum b : ParameterStyleEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_PARAMETER_STYLE = "parameter_style";
  private ParameterStyleEnum parameterStyle;

  public static final String JSON_PROPERTY_IS_DETERMINISTIC = "is_deterministic";
  private Boolean isDeterministic;

  /**
   * Function SQL data access.
   */
  public enum SqlDataAccessEnum {
    CONTAINS_SQL("CONTAINS_SQL"),
    
    READS_SQL_DATA("READS_SQL_DATA"),
    
    NO_SQL("NO_SQL");

    private String value;

    SqlDataAccessEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SqlDataAccessEnum fromValue(String value) {
      for (SqlDataAccessEnum b : SqlDataAccessEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_SQL_DATA_ACCESS = "sql_data_access";
  private SqlDataAccessEnum sqlDataAccess;

  public static final String JSON_PROPERTY_IS_NULL_CALL = "is_null_call";
  private Boolean isNullCall;

  /**
   * Function security type.
   */
  public enum SecurityTypeEnum {
    DEFINER("DEFINER");

    private String value;

    SecurityTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SecurityTypeEnum fromValue(String value) {
      for (SecurityTypeEnum b : SecurityTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_SECURITY_TYPE = "security_type";
  private SecurityTypeEnum securityType;

  public static final String JSON_PROPERTY_SPECIFIC_NAME = "specific_name";
  private String specificName;

  public static final String JSON_PROPERTY_COMMENT = "comment";
  private String comment;

  public static final String JSON_PROPERTY_PROPERTIES = "properties";
  private String properties;

  public static final String JSON_PROPERTY_FULL_NAME = "full_name";
  private String fullName;

  public static final String JSON_PROPERTY_CREATED_AT = "created_at";
  private Long createdAt;

  public static final String JSON_PROPERTY_UPDATED_AT = "updated_at";
  private Long updatedAt;

  public static final String JSON_PROPERTY_FUNCTION_ID = "function_id";
  private String functionId;

  public static final String JSON_PROPERTY_EXTERNAL_LANGUAGE = "external_language";
  private String externalLanguage;

  public FunctionInfo() { 
  }

  public FunctionInfo name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of function, relative to parent schema.
   * @return name
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getName() {
    return name;
  }


  @JsonProperty(JSON_PROPERTY_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setName(String name) {
    this.name = name;
  }


  public FunctionInfo catalogName(String catalogName) {
    this.catalogName = catalogName;
    return this;
  }

   /**
   * Name of parent catalog.
   * @return catalogName
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CATALOG_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getCatalogName() {
    return catalogName;
  }


  @JsonProperty(JSON_PROPERTY_CATALOG_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }


  public FunctionInfo schemaName(String schemaName) {
    this.schemaName = schemaName;
    return this;
  }

   /**
   * Name of parent schema relative to its parent catalog.
   * @return schemaName
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SCHEMA_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getSchemaName() {
    return schemaName;
  }


  @JsonProperty(JSON_PROPERTY_SCHEMA_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }


  public FunctionInfo inputParams(FunctionParameterInfos inputParams) {
    this.inputParams = inputParams;
    return this;
  }

   /**
   * Get inputParams
   * @return inputParams
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_INPUT_PARAMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public FunctionParameterInfos getInputParams() {
    return inputParams;
  }


  @JsonProperty(JSON_PROPERTY_INPUT_PARAMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setInputParams(FunctionParameterInfos inputParams) {
    this.inputParams = inputParams;
  }


  public FunctionInfo dataType(ColumnTypeName dataType) {
    this.dataType = dataType;
    return this;
  }

   /**
   * Get dataType
   * @return dataType
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_DATA_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public ColumnTypeName getDataType() {
    return dataType;
  }


  @JsonProperty(JSON_PROPERTY_DATA_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setDataType(ColumnTypeName dataType) {
    this.dataType = dataType;
  }


  public FunctionInfo fullDataType(String fullDataType) {
    this.fullDataType = fullDataType;
    return this;
  }

   /**
   * Pretty printed function data type.
   * @return fullDataType
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_FULL_DATA_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getFullDataType() {
    return fullDataType;
  }


  @JsonProperty(JSON_PROPERTY_FULL_DATA_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFullDataType(String fullDataType) {
    this.fullDataType = fullDataType;
  }


  public FunctionInfo returnParams(FunctionParameterInfos returnParams) {
    this.returnParams = returnParams;
    return this;
  }

   /**
   * Get returnParams
   * @return returnParams
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RETURN_PARAMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public FunctionParameterInfos getReturnParams() {
    return returnParams;
  }


  @JsonProperty(JSON_PROPERTY_RETURN_PARAMS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setReturnParams(FunctionParameterInfos returnParams) {
    this.returnParams = returnParams;
  }


  public FunctionInfo routineBody(RoutineBodyEnum routineBody) {
    this.routineBody = routineBody;
    return this;
  }

   /**
   * Function language. When **EXTERNAL** is used, the language of the routine function should be specified in the __external_language__ field,  and the __return_params__ of the function cannot be used (as **TABLE** return type is not supported), and the __sql_data_access__ field must be **NO_SQL**. 
   * @return routineBody
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ROUTINE_BODY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public RoutineBodyEnum getRoutineBody() {
    return routineBody;
  }


  @JsonProperty(JSON_PROPERTY_ROUTINE_BODY)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRoutineBody(RoutineBodyEnum routineBody) {
    this.routineBody = routineBody;
  }


  public FunctionInfo routineDefinition(String routineDefinition) {
    this.routineDefinition = routineDefinition;
    return this;
  }

   /**
   * Function body.
   * @return routineDefinition
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ROUTINE_DEFINITION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getRoutineDefinition() {
    return routineDefinition;
  }


  @JsonProperty(JSON_PROPERTY_ROUTINE_DEFINITION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRoutineDefinition(String routineDefinition) {
    this.routineDefinition = routineDefinition;
  }


  public FunctionInfo routineDependencies(DependencyList routineDependencies) {
    this.routineDependencies = routineDependencies;
    return this;
  }

   /**
   * Get routineDependencies
   * @return routineDependencies
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_ROUTINE_DEPENDENCIES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public DependencyList getRoutineDependencies() {
    return routineDependencies;
  }


  @JsonProperty(JSON_PROPERTY_ROUTINE_DEPENDENCIES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRoutineDependencies(DependencyList routineDependencies) {
    this.routineDependencies = routineDependencies;
  }


  public FunctionInfo parameterStyle(ParameterStyleEnum parameterStyle) {
    this.parameterStyle = parameterStyle;
    return this;
  }

   /**
   * Function parameter style. **S** is the value for SQL.
   * @return parameterStyle
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PARAMETER_STYLE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public ParameterStyleEnum getParameterStyle() {
    return parameterStyle;
  }


  @JsonProperty(JSON_PROPERTY_PARAMETER_STYLE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setParameterStyle(ParameterStyleEnum parameterStyle) {
    this.parameterStyle = parameterStyle;
  }


  public FunctionInfo isDeterministic(Boolean isDeterministic) {
    this.isDeterministic = isDeterministic;
    return this;
  }

   /**
   * Whether the function is deterministic.
   * @return isDeterministic
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_IS_DETERMINISTIC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Boolean getIsDeterministic() {
    return isDeterministic;
  }


  @JsonProperty(JSON_PROPERTY_IS_DETERMINISTIC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsDeterministic(Boolean isDeterministic) {
    this.isDeterministic = isDeterministic;
  }


  public FunctionInfo sqlDataAccess(SqlDataAccessEnum sqlDataAccess) {
    this.sqlDataAccess = sqlDataAccess;
    return this;
  }

   /**
   * Function SQL data access.
   * @return sqlDataAccess
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SQL_DATA_ACCESS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public SqlDataAccessEnum getSqlDataAccess() {
    return sqlDataAccess;
  }


  @JsonProperty(JSON_PROPERTY_SQL_DATA_ACCESS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSqlDataAccess(SqlDataAccessEnum sqlDataAccess) {
    this.sqlDataAccess = sqlDataAccess;
  }


  public FunctionInfo isNullCall(Boolean isNullCall) {
    this.isNullCall = isNullCall;
    return this;
  }

   /**
   * Function null call.
   * @return isNullCall
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_IS_NULL_CALL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Boolean getIsNullCall() {
    return isNullCall;
  }


  @JsonProperty(JSON_PROPERTY_IS_NULL_CALL)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setIsNullCall(Boolean isNullCall) {
    this.isNullCall = isNullCall;
  }


  public FunctionInfo securityType(SecurityTypeEnum securityType) {
    this.securityType = securityType;
    return this;
  }

   /**
   * Function security type.
   * @return securityType
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SECURITY_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public SecurityTypeEnum getSecurityType() {
    return securityType;
  }


  @JsonProperty(JSON_PROPERTY_SECURITY_TYPE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSecurityType(SecurityTypeEnum securityType) {
    this.securityType = securityType;
  }


  public FunctionInfo specificName(String specificName) {
    this.specificName = specificName;
    return this;
  }

   /**
   * Specific name of the function; Reserved for future use.
   * @return specificName
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SPECIFIC_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getSpecificName() {
    return specificName;
  }


  @JsonProperty(JSON_PROPERTY_SPECIFIC_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSpecificName(String specificName) {
    this.specificName = specificName;
  }


  public FunctionInfo comment(String comment) {
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


  public FunctionInfo properties(String properties) {
    this.properties = properties;
    return this;
  }

   /**
   * JSON-serialized key-value pair map, encoded (escaped) as a string.
   * @return properties
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_PROPERTIES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getProperties() {
    return properties;
  }


  @JsonProperty(JSON_PROPERTY_PROPERTIES)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setProperties(String properties) {
    this.properties = properties;
  }


  public FunctionInfo fullName(String fullName) {
    this.fullName = fullName;
    return this;
  }

   /**
   * Full name of function, in form of __catalog_name__.__schema_name__.__function__name__
   * @return fullName
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_FULL_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getFullName() {
    return fullName;
  }


  @JsonProperty(JSON_PROPERTY_FULL_NAME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }


  public FunctionInfo createdAt(Long createdAt) {
    this.createdAt = createdAt;
    return this;
  }

   /**
   * Time at which this function was created, in epoch milliseconds.
   * @return createdAt
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Long getCreatedAt() {
    return createdAt;
  }


  @JsonProperty(JSON_PROPERTY_CREATED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
  }


  public FunctionInfo updatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

   /**
   * Time at which this function was last updated, in epoch milliseconds.
   * @return updatedAt
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public Long getUpdatedAt() {
    return updatedAt;
  }


  @JsonProperty(JSON_PROPERTY_UPDATED_AT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setUpdatedAt(Long updatedAt) {
    this.updatedAt = updatedAt;
  }


  public FunctionInfo functionId(String functionId) {
    this.functionId = functionId;
    return this;
  }

   /**
   * Id of Function, relative to parent schema.
   * @return functionId
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_FUNCTION_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getFunctionId() {
    return functionId;
  }


  @JsonProperty(JSON_PROPERTY_FUNCTION_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setFunctionId(String functionId) {
    this.functionId = functionId;
  }


  public FunctionInfo externalLanguage(String externalLanguage) {
    this.externalLanguage = externalLanguage;
    return this;
  }

   /**
   * External language of the function.
   * @return externalLanguage
  **/
  @javax.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_EXTERNAL_LANGUAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getExternalLanguage() {
    return externalLanguage;
  }


  @JsonProperty(JSON_PROPERTY_EXTERNAL_LANGUAGE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setExternalLanguage(String externalLanguage) {
    this.externalLanguage = externalLanguage;
  }


  /**
   * Return true if this FunctionInfo object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FunctionInfo functionInfo = (FunctionInfo) o;
    return Objects.equals(this.name, functionInfo.name) &&
        Objects.equals(this.catalogName, functionInfo.catalogName) &&
        Objects.equals(this.schemaName, functionInfo.schemaName) &&
        Objects.equals(this.inputParams, functionInfo.inputParams) &&
        Objects.equals(this.dataType, functionInfo.dataType) &&
        Objects.equals(this.fullDataType, functionInfo.fullDataType) &&
        Objects.equals(this.returnParams, functionInfo.returnParams) &&
        Objects.equals(this.routineBody, functionInfo.routineBody) &&
        Objects.equals(this.routineDefinition, functionInfo.routineDefinition) &&
        Objects.equals(this.routineDependencies, functionInfo.routineDependencies) &&
        Objects.equals(this.parameterStyle, functionInfo.parameterStyle) &&
        Objects.equals(this.isDeterministic, functionInfo.isDeterministic) &&
        Objects.equals(this.sqlDataAccess, functionInfo.sqlDataAccess) &&
        Objects.equals(this.isNullCall, functionInfo.isNullCall) &&
        Objects.equals(this.securityType, functionInfo.securityType) &&
        Objects.equals(this.specificName, functionInfo.specificName) &&
        Objects.equals(this.comment, functionInfo.comment) &&
        Objects.equals(this.properties, functionInfo.properties) &&
        Objects.equals(this.fullName, functionInfo.fullName) &&
        Objects.equals(this.createdAt, functionInfo.createdAt) &&
        Objects.equals(this.updatedAt, functionInfo.updatedAt) &&
        Objects.equals(this.functionId, functionInfo.functionId) &&
        Objects.equals(this.externalLanguage, functionInfo.externalLanguage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, catalogName, schemaName, inputParams, dataType, fullDataType, returnParams, routineBody, routineDefinition, routineDependencies, parameterStyle, isDeterministic, sqlDataAccess, isNullCall, securityType, specificName, comment, properties, fullName, createdAt, updatedAt, functionId, externalLanguage);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FunctionInfo {\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    catalogName: ").append(toIndentedString(catalogName)).append("\n");
    sb.append("    schemaName: ").append(toIndentedString(schemaName)).append("\n");
    sb.append("    inputParams: ").append(toIndentedString(inputParams)).append("\n");
    sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
    sb.append("    fullDataType: ").append(toIndentedString(fullDataType)).append("\n");
    sb.append("    returnParams: ").append(toIndentedString(returnParams)).append("\n");
    sb.append("    routineBody: ").append(toIndentedString(routineBody)).append("\n");
    sb.append("    routineDefinition: ").append(toIndentedString(routineDefinition)).append("\n");
    sb.append("    routineDependencies: ").append(toIndentedString(routineDependencies)).append("\n");
    sb.append("    parameterStyle: ").append(toIndentedString(parameterStyle)).append("\n");
    sb.append("    isDeterministic: ").append(toIndentedString(isDeterministic)).append("\n");
    sb.append("    sqlDataAccess: ").append(toIndentedString(sqlDataAccess)).append("\n");
    sb.append("    isNullCall: ").append(toIndentedString(isNullCall)).append("\n");
    sb.append("    securityType: ").append(toIndentedString(securityType)).append("\n");
    sb.append("    specificName: ").append(toIndentedString(specificName)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("    fullName: ").append(toIndentedString(fullName)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    functionId: ").append(toIndentedString(functionId)).append("\n");
    sb.append("    externalLanguage: ").append(toIndentedString(externalLanguage)).append("\n");
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

    // add `schema_name` to the URL query string
    if (getSchemaName() != null) {
      joiner.add(String.format("%sschema_name%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getSchemaName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `input_params` to the URL query string
    if (getInputParams() != null) {
      joiner.add(getInputParams().toUrlQueryString(prefix + "input_params" + suffix));
    }

    // add `data_type` to the URL query string
    if (getDataType() != null) {
      joiner.add(String.format("%sdata_type%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getDataType()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `full_data_type` to the URL query string
    if (getFullDataType() != null) {
      joiner.add(String.format("%sfull_data_type%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getFullDataType()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `return_params` to the URL query string
    if (getReturnParams() != null) {
      joiner.add(getReturnParams().toUrlQueryString(prefix + "return_params" + suffix));
    }

    // add `routine_body` to the URL query string
    if (getRoutineBody() != null) {
      joiner.add(String.format("%sroutine_body%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getRoutineBody()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `routine_definition` to the URL query string
    if (getRoutineDefinition() != null) {
      joiner.add(String.format("%sroutine_definition%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getRoutineDefinition()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `routine_dependencies` to the URL query string
    if (getRoutineDependencies() != null) {
      joiner.add(getRoutineDependencies().toUrlQueryString(prefix + "routine_dependencies" + suffix));
    }

    // add `parameter_style` to the URL query string
    if (getParameterStyle() != null) {
      joiner.add(String.format("%sparameter_style%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getParameterStyle()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `is_deterministic` to the URL query string
    if (getIsDeterministic() != null) {
      joiner.add(String.format("%sis_deterministic%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getIsDeterministic()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `sql_data_access` to the URL query string
    if (getSqlDataAccess() != null) {
      joiner.add(String.format("%ssql_data_access%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getSqlDataAccess()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `is_null_call` to the URL query string
    if (getIsNullCall() != null) {
      joiner.add(String.format("%sis_null_call%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getIsNullCall()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `security_type` to the URL query string
    if (getSecurityType() != null) {
      joiner.add(String.format("%ssecurity_type%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getSecurityType()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `specific_name` to the URL query string
    if (getSpecificName() != null) {
      joiner.add(String.format("%sspecific_name%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getSpecificName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `comment` to the URL query string
    if (getComment() != null) {
      joiner.add(String.format("%scomment%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getComment()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `properties` to the URL query string
    if (getProperties() != null) {
      joiner.add(String.format("%sproperties%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getProperties()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `full_name` to the URL query string
    if (getFullName() != null) {
      joiner.add(String.format("%sfull_name%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getFullName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `created_at` to the URL query string
    if (getCreatedAt() != null) {
      joiner.add(String.format("%screated_at%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getCreatedAt()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `updated_at` to the URL query string
    if (getUpdatedAt() != null) {
      joiner.add(String.format("%supdated_at%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getUpdatedAt()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `function_id` to the URL query string
    if (getFunctionId() != null) {
      joiner.add(String.format("%sfunction_id%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getFunctionId()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `external_language` to the URL query string
    if (getExternalLanguage() != null) {
      joiner.add(String.format("%sexternal_language%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getExternalLanguage()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    return joiner.toString();
  }
}

