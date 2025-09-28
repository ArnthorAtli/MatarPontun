package is.hi.matarpontun.service;

import is.hi.matarpontun.model.Ward;
import is.hi.matarpontun.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WardService {

    @Autowired
    private WardRepository wardRepository;

    public Ward createWard(Ward ward) {
        return wardRepository.save(ward);
    }
    public List<Ward> findAllWards() {
        return wardRepository.findAll();
    }

    public Optional<Ward> signIn(String wardName, String password) {
        Optional<Ward> optionalWard = wardRepository.findByWardName(wardName);

        if (optionalWard.isPresent()) {
            Ward ward = optionalWard.get();
            //kanski bæta við hashing í framtíðinni...
            if (ward.getPassword().equals(password)) {
                return Optional.of(ward);
            }
        }
        return Optional.empty();
    }

    public Optional<Ward> signInWithData(String wardName, String password) {
        return wardRepository.findByWardNameAndPassword(wardName, password);
    }
}