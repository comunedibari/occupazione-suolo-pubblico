package it.fincons.osp.security.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import it.fincons.osp.model.Utente;
import lombok.Data;

@Data
public class UserDetailsImpl implements UserDetails {

	private static final long serialVersionUID = 5801289410012257669L;

	private Long id;

	private String username;

	private String email;

	@JsonIgnore
	private String password;

	private boolean enabled;

	private boolean flagEliminato;

	private Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(Long id, String username, String email, String password, boolean enabled,
			boolean flagEliminato, Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.enabled = enabled;
		this.flagEliminato = flagEliminato;
		this.authorities = authorities;
	}

	public static UserDetailsImpl build(Utente user) {
		GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getGruppo().getDescrizione());
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(grantedAuthority);

		return new UserDetailsImpl(user.getId(), user.getUsername(), user.getEmail(), user.getPassword(),
				user.isEnabled(), user.isFlagEliminato(), authorities);
	}

	@Override
	public boolean isAccountNonExpired() {
		return !flagEliminato;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDetailsImpl other = (UserDetailsImpl) obj;
		return Objects.equals(id, other.id);
	}

}
