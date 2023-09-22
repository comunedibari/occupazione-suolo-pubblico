package it.fincons.osp.services;

import it.fincons.osp.dto.AutomiProtocolloDTO;

import java.util.List;

public interface AutomiProtocolloService {
    List<AutomiProtocolloDTO> getAllAutomiProtocolloDTO();

    AutomiProtocolloDTO getAutomaProtocolloDTOByMunicipioId(Integer municipioId);

    AutomiProtocolloDTO getAutomaProtocolloDTOByid(Long automaId);

    AutomiProtocolloDTO update(AutomiProtocolloDTO automiProtocolloDTO);

    AutomiProtocolloDTO getAutomaProtocolloDTOByUoid(String uoid);
}
