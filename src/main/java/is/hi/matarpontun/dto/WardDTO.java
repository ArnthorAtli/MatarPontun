package is.hi.matarpontun.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WardDTO(
    Long id,
    String wardName,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password
)
{}
