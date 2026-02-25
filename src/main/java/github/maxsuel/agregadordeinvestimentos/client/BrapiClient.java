package github.maxsuel.agregadordeinvestimentos.client;

import github.maxsuel.agregadordeinvestimentos.dto.external.brapi.BrapiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "BrapiClient",
    url = "https://brapi.dev"
)
public interface BrapiClient {

    @GetMapping(value = "/api/quote/{stockId}")
    public BrapiResponseDto getQuote(@RequestParam("token") String token,
                                     @PathVariable("stockId") String stockId,
                                     @RequestParam(value = "modules", required = false) String modules);

}
