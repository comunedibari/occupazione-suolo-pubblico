package it.fincons.osp.mapper;

import it.fincons.osp.dto.*;
import it.fincons.osp.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AutomiProtocolloMapper {

	public AutomiProtocolloDTO entityToDto(AutomiProtocollo entity);

	public TypologicalDTO municipioToDto(Municipio entity);

}
