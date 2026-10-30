package com.code_challenge.release_manager.mapper

import com.code_challenge.release_manager.domain.AppsVersion
import com.code_challenge.release_manager.dto.AppsVersionDto
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface AppsVersionMapper {

    @Mapping(source = "name", target = "appsVersionId.name")
    @Mapping(source = "version", target = "appsVersionId.version")
    @Mapping(target = "creationDate", ignore = true)
    fun toEntity(dto: AppsVersionDto): AppsVersion

    fun toEntityList(dtoList: List<AppsVersionDto>): List<AppsVersion>


    @Mapping(source = "appsVersionId.name", target = "name")
    @Mapping(source = "appsVersionId.version", target = "version")
    fun toDto(entity: AppsVersion): AppsVersionDto

    fun toDtoList(entityList: List<AppsVersion>): List<AppsVersionDto>
}