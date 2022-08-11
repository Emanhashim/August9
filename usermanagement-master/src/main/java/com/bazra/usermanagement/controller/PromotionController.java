package com.bazra.usermanagement.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bazra.usermanagement.model.Account;
import com.bazra.usermanagement.model.Promotion;
import com.bazra.usermanagement.repository.AccountRepository;
import com.bazra.usermanagement.repository.PromotionRepository;
import com.bazra.usermanagement.request.CreatePromotionRequest;
import com.bazra.usermanagement.request.PromotionUpdateRequest;
import com.bazra.usermanagement.response.ListOfResponse;
import com.bazra.usermanagement.response.ResponseError;
import com.bazra.usermanagement.response.SinglePromotionResponse;
import com.bazra.usermanagement.response.SuccessMessageResponse;
import com.bazra.usermanagement.response.UpdateResponse;
import com.bazra.usermanagement.service.UserInfoService;


@RestController
@CrossOrigin("*")
@RequestMapping("/Api/Promotion")
public class PromotionController{
	@Autowired
	PromotionRepository promotionRepository;
	@Autowired
	AccountRepository accountRepository;
	
	@Value("${promotion.upload.path}")
	private String promophotoPath;
	
	@PostMapping("/CreatePromotion")

	public ResponseEntity<?> createPromotion(@ModelAttribute CreatePromotionRequest createPromotionRequest, Authentication authentication) throws IOException {
		Optional<Promotion> promotion = promotionRepository.findByTitle(createPromotionRequest.getTitle());
		Account adminAccount= accountRepository.findByAccountNumberEquals(authentication.getName()).get();
		
		if (!adminAccount.getType().matches("ADMIN")) {
			return ResponseEntity.badRequest().body(new ResponseError("Unauthorized request"));
		}
		if (promotion.isPresent()) {
			return ResponseEntity.badRequest().body(new ResponseError("Promotion exists!"));
		}
		String photoName = StringUtils.cleanPath(createPromotionRequest.getPicture().getOriginalFilename());
		String photouploadDir = promophotoPath;
		Promotion promo = new Promotion(createPromotionRequest.getTitle(),photoName, createPromotionRequest.getDescription(), createPromotionRequest.getExpirationDate(), createPromotionRequest.isStatus());
		promotionRepository.save(promo);
		UserInfoService.savePhoto(photouploadDir, photoName, createPromotionRequest.getPicture());
	     return ResponseEntity.ok(new SuccessMessageResponse("Created Promotion successfully!!"));
	}
	
	@PutMapping("/UpdatePromotion/{id}")

	public ResponseEntity<?> updatePromotion(@ModelAttribute PromotionUpdateRequest promotionUpdateRequest,Authentication authentication,@PathVariable int id) throws IOException {
		Account adminaccount = accountRepository.findByAccountNumberEquals(authentication.getName()).get();
		if (!adminaccount.getType().matches("ADMIN")) {
			return ResponseEntity.badRequest().body(new ResponseError("Unauthorized request"));

		}
		Optional<Promotion> optional = promotionRepository.findById(id);
		if (!optional.isPresent()) {
			return ResponseEntity.badRequest().body(new ResponseError("No promotion with this ID was found"));
		}
		Promotion promotion = optional.get();
		if(promotionUpdateRequest.getDescription()!=null) {
			promotion.setDescription(promotionUpdateRequest.getDescription());
		}
		if(promotionUpdateRequest.getExpirationDate()!=null) {
			promotion.setExpirationDate(promotionUpdateRequest.getExpirationDate());
		}
		if(promotionUpdateRequest.getPicture()!=null) {
			String photoName = StringUtils.cleanPath(promotionUpdateRequest.getPicture().getOriginalFilename());
			promotion.setPicture(photoName);
			String photouploadDir = promophotoPath;
			UserInfoService.savePhoto(photouploadDir, photoName, promotionUpdateRequest.getPicture());
		}
		if(promotionUpdateRequest.getTitle()!=null) {
			promotion.setTitle(promotionUpdateRequest.getTitle());
		}
		if(promotionUpdateRequest.isStatus()!= true) {
			promotion.setStatus(false);
		}
		if(promotionUpdateRequest.isStatus()!= false) {
			promotion.setStatus(true);
		}
		promotionRepository.save(promotion);
		return ResponseEntity.ok(new ResponseError("Updated promotion successfully!!!"));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletePromotion(@PathVariable Integer id, Authentication authentication) {
		Account adminaccount = accountRepository.findByAccountNumberEquals(authentication.getName()).get();
		if (!adminaccount.getType().matches("ADMIN")) {
			return ResponseEntity.badRequest().body(new ResponseError("Unauthorized request"));

		}
		if (promotionRepository.findById(id).isPresent()) {
			promotionRepository.deleteById(id);
			return ResponseEntity.ok(new UpdateResponse("Promotion Deleted successfully"));
		}
		else {
			return ResponseEntity.badRequest().body(new ResponseError("No Promotion found"));
		}
	}
	@GetMapping("/All")

	public ResponseEntity<?> all(@RequestParam Optional<String> sortBy,Authentication authentication) {
		
		List<Promotion> promotionsList = promotionRepository.findAll();
		if (promotionsList.isEmpty()) {
			return ResponseEntity.badRequest().body(new ResponseError("No Promotion found"));
		}
		return ResponseEntity.ok(new ListOfResponse(promotionsList));
	}
	@GetMapping("/All/{id}")
	public ResponseEntity<?> getPromotion(@PathVariable Integer id, Authentication authentication) throws IOException {
		Optional<Promotion> optional = promotionRepository.findById(id);
		if (!optional.isPresent()) {
			return ResponseEntity.badRequest().body(new ResponseError("No promotion found"));
		}
		
		String photouploadDir = promophotoPath + optional.get().getPicture();
	
	
		return ResponseEntity.ok(new SinglePromotionResponse(optional.get(),photouploadDir));
	}
	

}
