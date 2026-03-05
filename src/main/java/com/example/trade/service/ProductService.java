package com.example.trade.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.trade.dto.Address;
import com.example.trade.dto.Attachment;
import com.example.trade.dto.Category;
import com.example.trade.dto.CommTbl;
import com.example.trade.dto.Option;
import com.example.trade.dto.Order;
import com.example.trade.dto.Product;
import com.example.trade.dto.ProductRequest;
import com.example.trade.dto.ProductRequestForm;
import com.example.trade.mapper.ProductMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ProductService {
	private final ProductMapper productMapper;

	private static final String UPLOAD_DIR = "C:/uploads/product/";
	
	public ProductService(ProductMapper productMapper) {
		this.productMapper = productMapper;
	}
	
	// 개인 이름 조회
	public String selectName(String id) {
		return productMapper.selectName(id);
	}
	
	// 찜 개수 조회
	public int selectWishCount(String id) {
		return productMapper.wishCount(id);
	}
	
	// 장바구니 개수 조회
	public int selectCartCount(String id) {
		return productMapper.cartCount(id);
	}
	
	// 상품 후기 목록 보기
	public List<Map<String, Object>> selectReviewList() {
		return productMapper.reviewList();
	}
	
	// 상품 목록(찜 많은순)
	public List<Map<String, Object>> selectProductByWish() {
	    List<Map<String, Object>> productList = productMapper.productListByWish();
	    List<Map<String, Object>> imageList = productMapper.productMainImage();

	    Map<Integer, String> imageMap = new HashMap<>();
	    for (Map<String, Object> image : imageList) {
	        Integer productNo = ((Number) image.get("productNo")).intValue();
	        String filepath = (String) image.get("filepath");
	        imageMap.put(productNo, filepath);
	    }

	    for (Map<String, Object> product : productList) {
	        Integer productNo = ((Number) product.get("productNo")).intValue();
	        String filepath = imageMap.get(productNo);
	        product.put("imagePath", filepath);
	    }

	    return productList;
	}

	
	// 개인 찜 목록 보기
	public List<Map<String, Object>> selectWishList(String id) {
		List<Map<String, Object>> productList = productMapper.wishList(id);
	    List<Map<String, Object>> imageList = productMapper.productMainImage();

	    Map<Integer, String> imageMap = new HashMap<>();
	    for (Map<String, Object> image : imageList) {
	        Integer productNo = ((Number) image.get("productNo")).intValue();
	        String filepath = (String) image.get("filepath");
	        imageMap.put(productNo, filepath);
	    }

	    for (Map<String, Object> product : productList) {
	        Integer productNo = ((Number) product.get("productNo")).intValue();
	        String filepath = imageMap.get(productNo);
	        product.put("imagePath", filepath);
	    }

	    return productList;
	}
	
	// 개인 찜 삭제
	public void deleteWishItems(String loginUserName, List<Integer> productNoList) {
	    productMapper.deleteByUserNameAndProductNos(loginUserName, productNoList);
	}
	
	// 개인 장바구니 목록 보기
	public List<Map<String, Object>> selectShoppingCart(String id) {
		List<Map<String, Object>> productList = productMapper.shoppingCart(id);
	    List<Map<String, Object>> imageList = productMapper.productMainImage();

	    Map<Integer, String> imageMap = new HashMap<>();
	    for (Map<String, Object> image : imageList) {
	        Integer productNo = ((Number) image.get("productNo")).intValue();
	        String filepath = (String) image.get("filepath");
	        imageMap.put(productNo, filepath);
	    }

	    for (Map<String, Object> product : productList) {
	        Integer productNo = ((Number) product.get("productNo")).intValue();
	        String filepath = imageMap.get(productNo);
	        product.put("imagePath", filepath);
	        
	        // ✅ 재고 체크 후 수량 조정
	        int cartId = ((Number) product.get("cartId")).intValue();
	        int optionNo = ((Number) product.get("optionNo")).intValue();
	        int cartQty = ((Number) product.get("quantity")).intValue();
	        int inventoryQty = productMapper.currentStock(productNo, optionNo);
	        
	        product.put("inventoryQuantity", inventoryQty); // JSP에서 쓰이므로 유지
	        
	        if (cartQty > inventoryQty) {
	            // ✅ 수량 조정
	            product.put("quantity", inventoryQty);

	            // ✅ DB도 업데이트
	            productMapper.updateCartQuantity("admin", cartId, inventoryQty);

	            // ✅ 메시지용 (필요하면 메시지 리스트 따로 만들어 model에 전달 가능)
	            product.put("quantityAdjusted", true);
	        }
	    }

	    return productList;
	}
	
	// 개인 장바구니 수량 변경
	public boolean updateCartItemQuantity(String userId, int shoppingCartNo, int quantity) {
	    // 1. cartId로 productNo와 optionNo 모두 조회
	    Map<String, Integer> itemInfo = productMapper.findProductAndOptionByCartId(shoppingCartNo);
	    if (itemInfo == null) return false;

	    Integer productNo = itemInfo.get("productNo");
	    Integer optionNo = itemInfo.get("optionNo");
	    
	    // 2. 재고 수량 조회
	    Integer inventoryQuantity = productMapper.findInventoryQuantity(productNo, optionNo);
	    //log.info(inventoryQuantity+ "");
	    if (inventoryQuantity == null || quantity > inventoryQuantity) {
	        return false; // ❌ 재고 부족 또는 잘못된 요청
	    }
	    
		int updatedRows = productMapper.updateCartQuantity(userId, shoppingCartNo, quantity);
        return updatedRows > 0;
	}
	
	// 개인 장바구니 상품 삭제
	public boolean deleteCartItem(String userId, int cartId) {
	    int result = productMapper.deleteCartItemById(userId, cartId);
	    return result > 0;
	}

	// 장바구니에 담긴 상품 구매
	public String saveOrders(List<Order> orderList) {
		String orderNo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        int subOrderNo = 1;
        
        // 대표 배송지 받아오기
        int addressNo = productMapper.mainAddress(orderList.get(0).getUserId());
        
        for (Order order : orderList) {
        	// 최신 재고 조회
        	int currentStock = productMapper.currentStock(order.getProductNo(), order.getOptionNo());
        	
        	if (currentStock < order.getOrderQuantity()) {
                throw new IllegalArgumentException(
                    String.format("상품 [%s %s] 재고 부족 (요청: %d, 남은 재고: %d)",
                        order.getProductName(), order.getOptionNameValue(), order.getOrderQuantity(), currentStock));
            }
        	
        	order.setOrderNo(String.valueOf(orderNo));
            order.setSubOrderNo(String.valueOf(subOrderNo++));
            order.setOrderStatus("OS001");
            order.setDeliveryStatus("DS001");
            order.setCreateUser(order.getUserId());
            order.setAddressNo(addressNo);
            productMapper.insertOrder(order);
        }
        
        return String.valueOf(orderNo);
    }
	
	
	// 찜 토글
	public boolean toggleWish(String userId, int productNo, boolean wish) {
	    Map<String, Object> param = new HashMap<>();
	    param.put("userId", userId);
	    param.put("productNo", productNo);

	    Integer exists = productMapper.checkWishExists(param); // 0 or 1

	    if (exists == null || exists == 0) {
	        // 찜이 아예 없으면 insert
	        param.put("useStatus", wish ? "Y" : "N"); // 대부분 처음엔 Y
	        return productMapper.insertWish(param) > 0;
	    } else {
	        // 찜이 이미 있으면 update
	        param.put("useStatus", wish ? "Y" : "N");
	        return productMapper.updateWishStatus(param) > 0;
	    }
	}
	
	// 장바구니 추가
	public Map<String, Object> addCartItem(String userId, int productNo, int optionNo, int quantity) {
	    Map<String, Object> result = new HashMap<>();

	    int existing = productMapper.checkCart(userId, productNo, optionNo);

	    if (existing > 0) {
	        result.put("success", false);
	        result.put("message", "이미 장바구니에 담긴 상품입니다.");
	    } else {
	        boolean inserted = productMapper.insertCart(userId, productNo, optionNo, quantity) > 0;
	        result.put("success", inserted);
	        result.put("message", inserted ? "장바구니에 담겼습니다!" : "장바구니 추가에 실패했습니다.");
	    }

	    return result;
	}

	// 카테고리(대분류) 목록
	public List<Category> selectMajorCategory() {
		return productMapper.majorCategory();
	}
	
	// 카테고리(중분류) 목록
	public List<Category> selectMiddleCategory(String id) {
		return productMapper.middleCategory(id);
	}
	
	// 카테고리별 상품 목록 보기(판매중, 일시품절만)
	public List<Map<String, Object>> selectProductListByCategory(String parentId, String middleId) {
		List<Map<String, Object>> productList = productMapper.productListByCategory(parentId, middleId);
	    List<Map<String, Object>> imageList = productMapper.productMainImage();

	    Map<Integer, String> imageMap = new HashMap<>();
	    for (Map<String, Object> image : imageList) {
	        Integer productNo = ((Number) image.get("productNo")).intValue();
	        String filepath = (String) image.get("filepath");
	        imageMap.put(productNo, filepath);
	    }

	    for (Map<String, Object> product : productList) {
	        Integer productNo = ((Number) product.get("productNo")).intValue();
	        String filepath = imageMap.get(productNo);
	        product.put("imagePath", filepath);
	    }

	    return productList;
	}
	
	// 카테고리별 상품 목록 보기(전체)
	public List<Map<String, Object>> selectAllProductListByCategory(String parentId, String middleId) {
		List<Map<String, Object>> productList = productMapper.allProductListByCategory(parentId, middleId);
	    List<Map<String, Object>> imageList = productMapper.productMainImage();

	    Map<Integer, String> imageMap = new HashMap<>();
	    for (Map<String, Object> image : imageList) {
	        Integer productNo = ((Number) image.get("productNo")).intValue();
	        String filepath = (String) image.get("filepath");
	        imageMap.put(productNo, filepath);
	    }

	    for (Map<String, Object> product : productList) {
	        Integer productNo = ((Number) product.get("productNo")).intValue();
	        String filepath = imageMap.get(productNo);
	        product.put("imagePath", filepath);
	    }

	    return productList;
	}

	// 상품 상세 페이지 보기(개인용)
	public List<Map<String, Object>> selectPersonalProductOne(String id, int productNo) {
	    List<Map<String, Object>> productList = productMapper.personalProductOne(id, productNo);
	    List<Map<String, Object>> imageList = productMapper.productImage(productNo);
	    List<Map<String, Object>> detailImageList = productMapper.productDetailImage(productNo);
	    
	    Map<Integer, List<String>> imageMap = new HashMap<>();
	    for (Map<String, Object> image : imageList) {
	        String filepath = (String) image.get("filepath");
	        imageMap.computeIfAbsent(productNo, k -> new ArrayList<>()).add(filepath);
	    }
	    
	    Map<Integer, List<String>> detailImageMap = new HashMap<>();
	    for (Map<String, Object> detailImage : detailImageList) {
	        String detailFilepath = (String) detailImage.get("filepath");
	        detailImageMap.computeIfAbsent(productNo, k -> new ArrayList<>()).add(detailFilepath);
	    }

	    for (Map<String, Object> product : productList) {
	        List<String> filepaths = imageMap.get(productNo);
	        List<String> detailFilepaths = detailImageMap.get(productNo);
	        
	        product.put("imagePaths", filepaths);
	        product.put("detailImagePaths", detailFilepaths);
	    }

	    return productList;
	}
	
	// 상품 상세 페이지 보기(기업, 관리자용)
	public List<Map<String, Object>> selectProductOne(int productNo) {
	    List<Map<String, Object>> productList = productMapper.productOne(productNo);
	    List<Map<String, Object>> imageList = productMapper.productImage(productNo);
	    List<Map<String, Object>> detailImageList = productMapper.productDetailImage(productNo);
	    
	    Map<Integer, List<String>> imageMap = new HashMap<>();
	    for (Map<String, Object> image : imageList) {
	        String filepath = (String) image.get("filepath");
	        imageMap.computeIfAbsent(productNo, k -> new ArrayList<>()).add(filepath);
	    }
	    
	    Map<Integer, List<String>> detailImageMap = new HashMap<>();
	    for (Map<String, Object> detailImage : detailImageList) {
	        String detailFilepath = (String) detailImage.get("filepath");
	        detailImageMap.computeIfAbsent(productNo, k -> new ArrayList<>()).add(detailFilepath);
	    }

	    for (Map<String, Object> product : productList) {
	        List<String> filepaths = imageMap.get(productNo);
	        List<String> detailFilepaths = detailImageMap.get(productNo);
	        
	        product.put("imagePaths", filepaths);
	        product.put("detailImagePaths", detailFilepaths);
	    }

	    return productList;
	}
	
	// 상품별 리뷰 보기
	public List<Map<String, Object>> selectProductReview(int productNo) {
		return productMapper.productReview(productNo);
	}
	
	// 상품별 평균 평점
	public Double avgProductRate(int productNo) {
		return productMapper.avgProductRate(productNo);
	}
	
	// 기업회원 배송지
	public List<Address> selectBizAddress(String id) {
		return productMapper.bizAddress(id);
	}
	
	// 상품 요청 입력
	public void insertProductRequest(List<ProductRequest> list, List<MultipartFile> files) {
		
        // 첫 번째 상품 insert (subProductRequestNo = 1)
        ProductRequest first = list.get(0);
        first.setSubProductRequestNo(1);
        first.setUseStatus("Y");
        productMapper.insertProductRequest(first);
        
        // 자동 생성된 productRequestNo 가져오기
        int productRequestNo = first.getProductRequestNo();
        String createUser = first.getCreateUser();
        
        // 두 번째 상품부터는 같은 productRequestNo, subProductRequestNo 증가하면서 insert
        int subNo = 2;
        for (int i = 1; i < list.size(); i++) {
            ProductRequest pr = list.get(i);
            pr.setProductRequestNo(productRequestNo);
            pr.setSubProductRequestNo(subNo++);
            pr.setUseStatus("Y");
            productMapper.insertProductRequest(pr);
        }
        
        // 2. 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            int priority = 1;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String originalFileName = file.getOriginalFilename();
                        String uniqueFileName = UUID.randomUUID().toString().replace("-", "");
                        uniqueFileName += "_" + originalFileName;
                        
                        // 1) 로컬 디스크 저장
                        File saveFile = new File(UPLOAD_DIR + uniqueFileName);
                        saveFile.getParentFile().mkdirs();
                        file.transferTo(saveFile);

                        Attachment attachment = new Attachment();
                        attachment.setCategoryCode(productRequestNo);
                        attachment.setAttachmentCode("PRODUCT_REQUEST_FILE");
                        attachment.setFilename(originalFileName);
                        attachment.setFilepath("/uploads/product/" + uniqueFileName);
                        attachment.setUseStatus("Y");
                        attachment.setCreateUser(createUser);
                        attachment.setPriority(priority++);

                        productMapper.insertAttachment(attachment);

                    } catch (IOException e) {
                        throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }
	}
	
	// 상품 요청 리스트 조회
	public List<ProductRequest> selectProductRequestList() {
		return productMapper.productRequestList();
	}
	
	// 상품 요청 상세 조회
	public List<Map<String, Object>> selectProductRequestOne(int requestNo) {
		List<Map<String, Object>> requestDetails = productMapper.productRequestOne(requestNo);
		List<Map<String, Object>> attachmentList = productMapper.attachmentByRequestNo(requestNo);

	    Map<Integer, List<Map<String, Object>>> attachmentMap = new HashMap<>();
	    for (Map<String, Object> attach : attachmentList) {
	        Integer reqNo = (Integer) attach.get("requestNo");
	        String filepath = (String) attach.get("filepath");
	        String filename = (String) attach.get("filename");
	        Integer attachmentNo = (Integer) attach.get("attchmentNo");
	        
	        Map<String, Object> fileInfo = new HashMap<>();
	        fileInfo.put("filepath", filepath);
	        fileInfo.put("filename", filename);
	        fileInfo.put("attachmentNo", attachmentNo);
	        
	        attachmentMap.computeIfAbsent(reqNo, k -> new ArrayList<>()).add(fileInfo);
	    }

		for (Map<String, Object> detail : requestDetails) {
		    Integer reqNo = (Integer) detail.get("productRequestNo");
		    detail.put("attachments", attachmentMap.getOrDefault(reqNo, Collections.emptyList()));
		}

		return requestDetails;
	}
	
	// 상품 요청 첨부파일 삭제
	public int deleteAttachment(String userId, int attachmentNo) {
		return productMapper.deleteAttachment(userId, attachmentNo);
	}
	
	// 상품 요청 수정
	public void updateProductRequests(ProductRequestForm form, MultipartFile[] files) {
		List<ProductRequest> productList = form.getProductRequestList();
        int addressNo = form.getAddressNo();
        String requests = form.getRequests();

        for (ProductRequest pr : productList) {
            // 개별 상품 요청에 공통 필드 세팅
            pr.setAddressNo(addressNo);
            pr.setRequests(requests);
            //log.info(pr.toString());
            productMapper.updateProductRequest(pr);
        }
        
        int requestNo = productList.get(0).getProductRequestNo();
        String createUser = productList.get(0).getCreateUser();
        
        Integer maxPriority = productMapper.findMaxPriorityByRequestNo(requestNo);
	    int priority = (maxPriority != null) ? maxPriority + 1 : 1;
	    
		for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    // 1. 원본 파일명
                    String originalFileName = file.getOriginalFilename();

                    // 2. 고유한 파일명 생성 (중복 방지)
                    String uniqueFileName = UUID.randomUUID().toString().replace("-", "");
                    uniqueFileName += "_" + originalFileName;
                    
                    // 3. 저장할 경로 생성
                    File saveFile = new File(UPLOAD_DIR + uniqueFileName);
                    // 디렉토리가 없으면 생성
                    saveFile.getParentFile().mkdirs();
                    // 4. 로컬에 파일 저장
                    file.transferTo(saveFile);

                    // 5. DB 저장
                    Attachment attachment = new Attachment();
                    attachment.setCategoryCode(requestNo);
                    attachment.setAttachmentCode("PRODUCT_REQUEST_FILE");
                    attachment.setFilename(originalFileName); // 원본 파일명
                    attachment.setFilepath("/uploads/product/" + uniqueFileName); // 웹에서 접근 가능한 경로
                    attachment.setUseStatus("Y");
                    attachment.setCreateUser(createUser); // 또는 로그인 유저 ID
                    attachment.setPriority(priority++);
                    
                    productMapper.insertAttachment(attachment);

                } catch (IOException e) {
                    throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                }
            }
        }
	}
	
	// 상품 요청 삭제
	public void deleteProductRequest(String userId, int requestNo) {
		productMapper.deleteProductRequest(userId, requestNo);
		productMapper.deleteProductAttachment(userId, requestNo);
	}
	
	// 카테고리 추가
	public Category insertCategory(Category category) {
		String parentId = category.getParentCategory();
		
		if ("0".equals(parentId)) {
		    // 대분류 처리
		    String maxIdStr = productMapper.selectMaxMajorCategoryId();
		    int newId = (maxIdStr != null) ? Integer.parseInt(maxIdStr) + 1 : 1;
		    category.setCategoryId(String.valueOf(newId));

		} else if (parentId.length() == 1) {
		    // 중분류 처리 (예: parentId = "1", newId는 "1001", "1002", ...)
		    String maxSubIdStr = productMapper.selectMaxSubCategoryId(parentId);
		    int nextSubNum = 1;

		    if (maxSubIdStr != null && maxSubIdStr.startsWith(parentId)) {
		        String suffix = maxSubIdStr.substring(parentId.length()); // "002"
		        nextSubNum = Integer.parseInt(suffix) + 1;
		    }

		    String newId = parentId + String.format("%03d", nextSubNum);
		    category.setCategoryId(newId);

		} else if (parentId.length() == 4) {
		    // 소분류 처리 (예: parentId = "1002", newId는 "1002001", "1002002", ...)
		    String maxSubIdStr = productMapper.selectMaxSubCategoryId(parentId);
		    int nextSubNum = 1;

		    if (maxSubIdStr != null && maxSubIdStr.startsWith(parentId)) {
		        String suffix = maxSubIdStr.substring(parentId.length()); // "001"
		        nextSubNum = Integer.parseInt(suffix) + 1;
		    }

		    String newId = parentId + String.format("%03d", nextSubNum);
		    category.setCategoryId(newId);

		}

		productMapper.insertCategory(category);
		return category;
	}
	
	// 옵션 목록 보기
	public List<Option> selectOptionList() {
		return productMapper.optionList();
	}
	
	// 옵션 추가
	public void insertOption(Option option) {
		productMapper.insertOption(option);
	}
	
	// 같은 상품명, 옵션 있는지 확인
	public boolean isProductOptionDuplicated(String productName, Integer optionNo) {
        // DB에서 productName + optionNo 가 존재하는지 체크
        return productMapper.existsByProductNameAndOptionNo(productName, optionNo);
    }
	
	// 상품 등록
	public void insertProduct(Product product, List<MultipartFile> productImages, List<MultipartFile> detailImages) {
        // 1. product_no 설정
        Integer existingProductNo = productMapper.findProductNoByName(product.getProductName());

        int resolvedProductNo;
        if (existingProductNo != null) {
            resolvedProductNo = existingProductNo; // 기존 product 사용
        } else {
            Integer maxProductNo = productMapper.findMaxProductNo();
            resolvedProductNo = (maxProductNo != null) ? maxProductNo + 1 : 1;
        }

        product.setProductNo(resolvedProductNo);
		productMapper.insertProduct(product);
		
		// 2. 이미지 있다면 이미지 삽입
	    int priority = 1;
	    
		for (MultipartFile file : productImages) {
            if (!file.isEmpty()) {
                try {
                    // 1. 원본 파일명
                    String originalFileName = file.getOriginalFilename();

                    // 2. 고유한 파일명 생성 (중복 방지)
                    String uniqueFileName = UUID.randomUUID().toString().replace("-", "");
                    uniqueFileName += "_" + originalFileName;
                    
                    // 3. 저장할 경로 생성
                    File saveFile = new File(UPLOAD_DIR + uniqueFileName);
                    // 디렉토리가 없으면 생성
                    saveFile.getParentFile().mkdirs();
                    // 4. 로컬에 파일 저장
                    file.transferTo(saveFile);

                    // 5. DB 저장
                    Attachment attachment = new Attachment();
                    attachment.setCategoryCode(resolvedProductNo); // 상품 번호
                    attachment.setAttachmentCode("PRODUCT_IMAGE");
                    attachment.setFilename(originalFileName); // 원본 파일명
                    attachment.setFilepath("/uploads/product/" + uniqueFileName); // 웹에서 접근 가능한 경로
                    attachment.setUseStatus("Y");
                    attachment.setCreateUser(product.getCreateUser());// 또는 로그인 유저 ID
                    attachment.setPriority(priority++);
                    
                    productMapper.insertAttachment(attachment);

                } catch (IOException e) {
                    throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                }
            }
        }
		
		// 3. 상세 이미지 처리
		if (detailImages != null && !detailImages.isEmpty()) {
		    int detailPriority = 1;

		    for (MultipartFile file : detailImages) {
		        if (!file.isEmpty()) {
		            try {
		                // 1. 원본 파일명
		                String originalFileName = file.getOriginalFilename();

		                // 2. 고유한 파일명 생성
		                String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + "_" + originalFileName;

		                // 3. 저장 경로
		                File saveFile = new File(UPLOAD_DIR + uniqueFileName);
		                saveFile.getParentFile().mkdirs(); // 디렉토리 없으면 생성
		                file.transferTo(saveFile); // 파일 저장

		                // 5. DB 저장
		                Attachment attachment = new Attachment();
		                attachment.setCategoryCode(resolvedProductNo);
		                attachment.setAttachmentCode("PRODUCT_DETAIL"); // ✅ 상세이미지로 구분
		                attachment.setFilename(originalFileName);
		                attachment.setFilepath("/uploads/product/" + uniqueFileName);
		                attachment.setUseStatus("Y");
		                attachment.setCreateUser(product.getCreateUser());
		                attachment.setPriority(detailPriority++);

		                productMapper.insertAttachment(attachment);

		            } catch (IOException e) {
		                throw new RuntimeException("상세 이미지 업로드 실패: " + file.getOriginalFilename(), e);
		            }
		        }
		    }
		}

		
		// 3. 재고 테이블에 초기 재고(0) 등록
		productMapper.insertInventory(resolvedProductNo, product.getOptionNo());
	}

	// 상품 이미지 등록
	public void insertProductImages(int productNo, List<MultipartFile> imageFiles, String loginUserName) {
		Integer maxPriority = productMapper.findMaxPriorityByCategoryCode(productNo);
	    int priority = (maxPriority != null) ? maxPriority + 1 : 1;
	    
		for (MultipartFile file : imageFiles) {
            if (!file.isEmpty()) {
                try {
                    // 1. 원본 파일명
                    String originalFileName = file.getOriginalFilename();

                    // 2. 고유한 파일명 생성 (중복 방지)
                    String uniqueFileName = UUID.randomUUID().toString().replace("-", "");
                    uniqueFileName += "_" + originalFileName;
                    
                    // 3. 저장할 경로 생성
                    File saveFile = new File(UPLOAD_DIR + uniqueFileName);
                    // 디렉토리가 없으면 생성
                    saveFile.getParentFile().mkdirs();
                    // 4. 로컬에 파일 저장
                    file.transferTo(saveFile);

                    // 5. DB 저장
                    Attachment attachment = new Attachment();
                    attachment.setCategoryCode(productNo); // 상품 번호
                    attachment.setAttachmentCode("PRODUCT_IMAGE");
                    attachment.setFilename(originalFileName); // 원본 파일명
                    attachment.setFilepath("/uploads/product/" + uniqueFileName); // 웹에서 접근 가능한 경로
                    attachment.setUseStatus("Y");
                    attachment.setCreateUser(loginUserName); // 또는 로그인 유저 ID
                    attachment.setPriority(priority++);
                    
                    productMapper.insertAttachment(attachment);

                } catch (IOException e) {
                	 e.printStackTrace();
                    throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                }
            }
        }
	}
	
	// 상품 상세 등록
	public void insertDetailProductImages(int productNo, List<MultipartFile> imageFiles, String loginUserName) {
		Integer maxPriority = productMapper.findMaxDetailPriorityByCategoryCode(productNo);
	    int priority = (maxPriority != null) ? maxPriority + 1 : 1;
	    
		for (MultipartFile file : imageFiles) {
            if (!file.isEmpty()) {
                try {
                    // 1. 원본 파일명
                    String originalFileName = file.getOriginalFilename();

                    // 2. 고유한 파일명 생성 (중복 방지)
                    String uniqueFileName = UUID.randomUUID().toString().replace("-", "");
                    uniqueFileName += "_" + originalFileName;
                    
                    // 3. 저장할 경로 생성
                    File saveFile = new File(UPLOAD_DIR + uniqueFileName);
                    // 디렉토리가 없으면 생성
                    saveFile.getParentFile().mkdirs();
                    // 4. 로컬에 파일 저장
                    file.transferTo(saveFile);

                    // 5. DB 저장
                    Attachment attachment = new Attachment();
                    attachment.setCategoryCode(productNo); // 상품 번호
                    attachment.setAttachmentCode("PRODUCT_DETAIL");
                    attachment.setFilename(originalFileName); // 원본 파일명
                    attachment.setFilepath("/uploads/product/" + uniqueFileName); // 웹에서 접근 가능한 경로
                    attachment.setUseStatus("Y");
                    attachment.setCreateUser(loginUserName); // 또는 로그인 유저 ID
                    attachment.setPriority(priority++);
                    
                    productMapper.insertAttachment(attachment);

                } catch (IOException e) {
                	 e.printStackTrace();
                    throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
                }
            }
        }
	}
	
	// 상품 상태 리스트 조회
	public List<CommTbl> getProductStatusCode() {
		return productMapper.productStatusCode();
	}
	
	// 상품 상태 변경
	public void updateProductStatus(String userId, int productNo, String productStatus) {
		productMapper.updateProductStatus(userId, productNo, productStatus);
	}
	
	// 상품 옵션 가격 변경
	public void updateProductOptionPrice(String userId, int productNo, int optionNo, int price) {
		productMapper.updateProductOptionPrice(userId, productNo, optionNo, price);
	}
	
	// 상품 이미지 삭제
	public void deleteProductImage(String userId, int productNo, String imagePath) {
		productMapper.deleteProductImage(userId, productNo, imagePath);
	}
	
	// 재고 조회
	public List<Map<String, Object>> selectInventoryList() {
		return productMapper.inventoryList();
	}
	
	// 재고 수정
	public void updateInventoryQuantity(String userId, int inventoryId, int quantity, int productNo, int optionNo) {
		// 수량이 0이 아니면 -> 판매상태 판매중으로
		if (quantity != 0) {
			productMapper.updateProductAndOptionStatus(userId, productNo, optionNo, "GS002");
		} else { // 수량이 0이면 -> 판매상태 일시품절으로
			productMapper.updateProductAndOptionStatus(userId, productNo, optionNo, "GS003");
		}
		productMapper.updateInventoryQuantity(inventoryId, quantity);
	}
	
	// 상품 사용여부 변경
	public void changeProductUseStatus(String userId, int productNo, String useStatus) {
		productMapper.updateProductUseStatus(userId, productNo, useStatus);
	}
	
	// 창고 주소 불러오기
	public List<Address> selectWarehouse(String id) {
		return productMapper.warehouse(id);
	}
	
	// 재고 창고 등록
	public void updateInventoryAddress(int inventoryId, int addressNo) {
		productMapper.updateInventoryAddress(inventoryId, addressNo);
	}
	
	// 관리자 카테고리 추가/수정
	public List<Map<String, Object>> selectAllCategory() {
		return productMapper.allCategory();
	}
	 
	// 카테고리 이름 수정
	public void updateCategoryName(int categoryId, String newName, String loginUser) {
	    productMapper.updateCategoryName(categoryId, newName, loginUser);
	}

	// 카테고리 삭제
	public boolean removeCategory(int categoryId, String loginUser) {
		int affected = productMapper.deleteCategory(categoryId, loginUser);
        return affected > 0; // 삭제된 row가 1 이상이면 성공
	}
	
	// 옵션 그룹 수정
	public void updateOptionGroupName(String optionGroupName, String newName, String loginUser) {
		productMapper.updateOptionGroupName(optionGroupName, newName, loginUser);
	}
		
	// 옵션명 수정
	public void updateOptionName(int optionNo, String newName, String loginUser) {
	    productMapper.updateOptionName(optionNo, newName, loginUser);
	}
	
	// 옵션 그룹 삭제
	public void removeOptionGroup(String optionGroupName, String loginUser) {
		productMapper.removeOptionGroup(optionGroupName, loginUser);
	}
	
	// 옵션 삭제
	public void removeOption(int optionNo, String loginUser) {
		productMapper.removeOption(optionNo, loginUser);
	}
	
}
