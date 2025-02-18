package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import net.wasys.getdoc.domain.enumeration.FaceRecognitionApi;

@Entity(name="FACE_RECOGNITION")
public class FaceRecognition extends net.wasys.util.ddd.Entity {

	private Long id;
	private FaceRecognitionApi api;
	private String imagePath;
	private Integer facePositionXc;
	private Integer facePositionYc;
	private Integer facePositionW;
	private Double facePositionAngle;
	private Integer faceListId;
	private Integer eye1X;
	private Integer eye1Y;
	private Integer eye2X;
	private Integer eye2Y;
	private byte[] template;
	private byte[] faceImage;
	private byte[] image;
	private byte[] imageRecogn;
	private String imgUid;
	private String nome;

	private Imagem imagem;
	private String faceUid;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="NOME")
	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="API")
	public FaceRecognitionApi getApi() {
		return api;
	}

	public void setApi(FaceRecognitionApi api) {
		this.api = api;
	}

	@Column(name="IMAGE_PATH")
	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@Column(name="FACE_POSITION_XC")
	public Integer getFacePositionXc() {
		return facePositionXc;
	}

	public void setFacePositionXc(Integer facePositionXc) {
		this.facePositionXc = facePositionXc;
	}

	@Column(name="FACE_POSITION_YC")
	public Integer getFacePositionYc() {
		return facePositionYc;
	}

	public void setFacePositionYc(Integer facePositionYc) {
		this.facePositionYc = facePositionYc;
	}

	@Column(name="FACE_POSITION_W")
	public Integer getFacePositionW() {
		return facePositionW;
	}

	public void setFacePositionW(Integer facePositionW) {
		this.facePositionW = facePositionW;
	}

	@Column(name="FACE_POSITION_ANGLE")
	public Double getFacePositionAngle() {
		return facePositionAngle;
	}

	public void setFacePositionAngle(Double facePositionAngle) {
		this.facePositionAngle = facePositionAngle;
	}

	@Column(name="EYE_1X")
	public Integer getEye1X() {
		return eye1X;
	}

	public void setEye1X(Integer eye1x) {
		this.eye1X = eye1x;
	}

	@Column(name="EYE_1Y")
	public Integer getEye1Y() {
		return eye1Y;
	}

	public void setEye1Y(Integer eye1y) {
		this.eye1Y = eye1y;
	}

	@Column(name="EYE_2X")
	public Integer getEye2X() {
		return eye2X;
	}

	public void setEye2X(Integer eye2x) {
		this.eye2X = eye2x;
	}

	@Column(name="EYE_2Y")
	public Integer getEye2Y() {
		return eye2Y;
	}

	public void setEye2Y(Integer eye2Y) {
		this.eye2Y = eye2Y;
	}

	@Column(name="FACE_LIST_ID")
	public Integer getFaceListId() {
		return faceListId;
	}

	public void setFaceListId(Integer faceListId) {
		this.faceListId = faceListId;
	}

	@Column(name="TEMPLATE")
	public byte[] getTemplate() {
		return template;
	}

	public void setTemplate(byte[] template) {
		this.template = template;
	}

	@Column(name="FACE_IMAGE")
	public byte[] getFaceImage() {
		return faceImage;
	}

	public void setFaceImage(byte[] faceImage) {
		this.faceImage = faceImage;
	}

	@Column(name="IMAGE")
	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	@Column(name="IMAGE_RECOGN")
	public byte[] getImageRecogn() {
		return imageRecogn;
	}

	public void setImageRecogn(byte[] imageRecogn) {
		this.imageRecogn = imageRecogn;
	}

	@Column(name="IMG_UID")
	public String getImgUid() {
		return imgUid;
	}

	public void setImgUid(String imgUid) {
		this.imgUid = imgUid;
	}

	@Column(name="FACE_UID")
	public String getFaceUid() {
		return faceUid;
	}

	public void setFaceUid(String faceUid) {
		this.faceUid = faceUid;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IMAGEM_ID", nullable=false)
	public Imagem getImagem() {
		return imagem;
	}

	public void setImagem(Imagem imagem) {
		this.imagem = imagem;
	}
}
