package it.fincons.osp.security.services;

import it.fincons.osp.exceptions.BusinessException;
import it.fincons.osp.exceptions.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.fincons.osp.model.Utente;
import it.fincons.osp.repository.UtenteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	UtenteRepository utenteRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		List<Utente> utenti = utenteRepository.findByUsername(username);
		Utente utente=null;

		if(utenti==null||utenti.size()==0){
			throw new UsernameNotFoundException("Utente non trovato con username: " + username);
		}

		if(utenti!=null&&utenti.size()==1){
			utente=utenti.get(0);
		}else{
			boolean found=false;

			for(Utente _utente:utenti){
				if(!_utente.isFlagEliminato()){
					utente=_utente;
					break;
				}
			}
		}

		if(utente==null){
			throw new UsernameNotFoundException("Utente non trovato con username: " + username);
		}

		UserDetailsImpl userDetails= UserDetailsImpl.build(utente);

		if(utente.isFlagEliminato()){
			userDetails.setEnabled(true);
		}

		return userDetails;
	}

}
