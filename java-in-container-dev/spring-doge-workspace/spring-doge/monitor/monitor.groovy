import com.pi4j.io.gpio.*
import org.springframework.web.client.*

@Configuration
@EnableScheduling
@Grab("com.pi4j:pi4j-core:0.0.5")
@Grab("spring-web")
class EverySecond {

	GpioPinDigitalOutput pin1 = GpioFactory.getInstance()
		.provisionDigitalOutputPin(RaspiPin.GPIO_01)

	RestTemplate rest = new RestTemplate()

	@Value('${url}') String url

	@PostConstruct
	void setup() {
		pin1.low()
	}

	@Scheduled(fixedRate=1000l)
	void check() {
		try {
			def status = rest.getForObject(url, String).toString()
						if (!status.contains("UP")) raiseAlert()
		} catch (Exception ex) {
			ex.printStackTrace()
			raiseAlert()
		}
	}

	void raiseAlert() {
		"""aplay sound.wav""".execute()
                for(int i=0;i<20;i++) {
			pin1.high()
			Thread.sleep(500)
			pin1.low()
			Thread.sleep(500)
		}
		System.exit(0)
	}
}
