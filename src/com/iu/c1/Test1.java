#include "stdafx.h"
#include "Camera_Dynamic.h"
#include "GameInstance.h"
#include "Layer.h"
#include "Player.h"
#include "Gun.h"

#ifdef _DEBUG
#include "ImGui_Manager.h"
#endif

extern	mt19937	g_Random;

void CCamera_Dynamic::Set_CameMove(_bool isMove)
{
	m_bMoveCamera = isMove;

	if (true == m_bStart_ShowBoss2)
		m_bStart_ShowBoss2 = false;

	if (true == m_bStart_ShowBoss1)
		m_bStart_ShowBoss1 = false;
}

CCamera_Dynamic::CCamera_Dynamic(ID3D11Device* pDevice, ID3D11DeviceContext* pDeviceContext)
	: CCamera(pDevice, pDeviceContext)
{
}

CCamera_Dynamic::CCamera_Dynamic(const CCamera_Dynamic & rhs)
	: CCamera(rhs)
{
}

HRESULT CCamera_Dynamic::NativeConstruct_Prototype()
{
	if (FAILED(__super::NativeConstruct_Prototype()))
		return E_FAIL;

	return S_OK;
}

HRESULT CCamera_Dynamic::NativeConstruct(void * pArg)
{
	if (FAILED(__super::NativeConstruct(pArg)))
		return E_FAIL;

	if (FAILED(SetUp_Components()))
		return E_FAIL;

	GAME_INSTANCE;

	if (FAILED(pGameInstance->Add_Timer(TEXT("Camera_Timer"))))
		return E_FAIL;

	GAME_RELEASE;

	return S_OK;
}

FRESULT CCamera_Dynamic::Tick(_double TimeDelta)
{
	GAME_INSTANCE;

	m_TimeDelta = pGameInstance->Get_TimeDelta(TEXT("Camera_Timer"));

#ifdef _DEBUG
	ImGui_Camera(m_TimeDelta);

	HRESULT hr = 0;

	if (LEVEL_TOOL == pGameInstance->Get_CurrentLevelIndex())
		hr = Level_Tool(m_TimeDelta);
	else
		hr = Level_GamePlay(m_TimeDelta);

	if (FAILED(hr))
		return F_FAILED;
#else
	if (FAILED(Level_GamePlay(m_TimeDelta)))
		return F_FAILED;
#endif

	GAME_RELEASE;

	if (true == m_isShaking) Camera_Shake(m_TimeDelta);
	if (true == m_isNervous) Camera_Nervous(m_TimeDelta);

	return __super::Tick(m_TimeDelta);
}

FRESULT CCamera_Dynamic::LateTick(_double TimeDelta)
{
	if (0 > __super::LateTick(m_TimeDelta))
		return F_FAILED;

	return F_OK;
}

HRESULT CCamera_Dynamic::Render()
{
	return S_OK;
}

HRESULT CCamera_Dynamic::SetUp_Components()
{
	m_pPivotTransformCom = CTransform::Create(m_pDevice, m_pDeviceContext);
	m_pRotTransformCom = CTransform::Create(m_pDevice, m_pDeviceContext);

	CTransform::TRANSFORMDESC TransformDesc;
	TransformDesc.fRotationPerSec = XMConvertToRadians(50.f);

	m_pPivotTransformCom->Set_TransformDesc(&TransformDesc);
	m_pRotTransformCom->Set_TransformDesc(&TransformDesc);

	return S_OK;
}

HRESULT CCamera_Dynamic::Level_GamePlay(_double dTimeDelta)
{
	GAME_INSTANCE;

	if (false == m_bMoveCamera)
	{
		CTransform* pPlayerTranform = static_cast<CTransform*>(pGameInstance->Get_Component(pGameInstance->Get_CurrentLevelIndex(), TEXT("Layer_Player"), CGameObject::m_pTransformTag));
		_vector vPosition = XMVectorSetY(pPlayerTranform->Get_State(CTransform::STATE_POS), XMVectorGetY(pPlayerTranform->Get_State(CTransform::STATE_POS)) + 1.5f);
		m_pPivotTransformCom->Set_State(CTransform::STATE_POS, vPosition);

		_long MouseMove = 0;

		if (MouseMove = pGameInstance->Get_DIMouseMoveState(CInput_Device::MMS_X))
		{
			m_pPivotTransformCom->Turn(XMVectorSet(0.f, 1.f, 0.f, 0.f), dTimeDelta * (_double)MouseMove * 0.05);
			m_pRotTransformCom->Turn(XMVectorSet(0.f, 1.f, 0.f, 0.f), dTimeDelta * (_double)MouseMove * 0.05);
		}

		if (MouseMove = pGameInstance->Get_DIMouseMoveState(CInput_Device::MMS_Y))
		{
			if (0 > MouseMove)
			{
				if (true == Limit_Rotation(XMVectorSet(0.f, 1.f, 0.f, 0.f), 80.f))
					m_pPivotTransformCom->Turn(m_pPivotTransformCom->Get_State(CTransform::STATE_RIGHT), dTimeDelta * (_double)MouseMove * 0.05);
			}
			else
			{
				if (true == Limit_Rotation(XMVectorSet(0.f, -1.f, 0.f, 0.f), 35.f))
					m_pPivotTransformCom->Turn(m_pPivotTransformCom->Get_State(CTransform::STATE_RIGHT), dTimeDelta * (_double)MouseMove * 0.05);
			}
		}

		_vector vPos = m_pPivotTransformCom->Get_State(CTransform::STATE_POS) - m_pPivotTransformCom->Get_State(CTransform::STATE_LOOK) * 5.f;

		m_pTransform->Set_State(CTransform::STATE_POS, vPos);
		m_pTransform->LookAt(m_pPivotTransformCom->Get_State(CTransform::STATE_POS));
	}
	else if (true == m_bStart_ShowGun)
	{
		Show_Gun(dTimeDelta);
	}
	else if (true == m_bStart_PlayerChange)
	{
		Show_PlayerChange(dTimeDelta);
	}
	else if (true == m_bStart_ShowBoss2)
	{
		Show_Boss2(dTimeDelta);
	}
	else if (true == m_bBack_Camera_Boss2)
	{
		Back_Boss2(dTimeDelta);
	}
	else if (true == m_bBack_Camera_Boss1)
	{
		Back_Boss1(dTimeDelta);
	}

	GAME_RELEASE;

	return S_OK;
}

HRESULT CCamera_Dynamic::Level_Tool(_double dTimeDelta)
{
	GAME_INSTANCE;

	if (pGameInstance->Get_DIKeyState(DIK_W) & 0x80)
	{
		m_pTransform->Go_Straight(dTimeDelta);
	}

	if (pGameInstance->Get_DIKeyState(DIK_S) & 0x80)
	{
		m_pTransform->Go_Backward(dTimeDelta);
	}

	if (pGameInstance->Get_DIKeyState(DIK_A) & 0x80)
	{
		m_pTransform->Go_Left(dTimeDelta);
	}

	if (pGameInstance->Get_DIKeyState(DIK_D) & 0x80)
	{
		m_pTransform->Go_Right(dTimeDelta);
	}

	if (pGameInstance->Get_DIKey_Down(DIK_P))
		true == m_bMoveCamera ? m_bMoveCamera = false : m_bMoveCamera = true;

	if (true == m_bMoveCamera)
	{
		_long		MouseMove = 0;

		if (MouseMove = pGameInstance->Get_DIMouseMoveState(CInput_Device::MMS_X))
		{
			m_pTransform->Turn(XMVectorSet(0.f, 1.f, 0.f, 0.f), dTimeDelta * MouseMove * 0.1);
		}

		if (MouseMove = pGameInstance->Get_DIMouseMoveState(CInput_Device::MMS_Y))
		{
			m_pTransform->Turn(m_pTransform->Get_State(CTransform::STATE_RIGHT), dTimeDelta * MouseMove * 0.1);
		}
	}

	GAME_RELEASE;

	return S_OK;
}

void CCamera_Dynamic::Start_Shaking(_float fFinishTime, _float fShakeRange, _bool bHasPos)
{
	m_isShaking = true;
	m_bHasPos = bHasPos;
	m_fFinishShakeTime = fFinishTime;
	m_fShakeRange = fShakeRange;
	XMStoreFloat3(&m_vPrevCamPos, m_pTransform->Get_State(CTransform::STATE_POS));
}

void CCamera_Dynamic::Start_Nervous(_float fFinishTime, _float fNervousRange)
{
	m_isNervous = true;
	m_fFinishNervousTime = fFinishTime;
	m_fNervousRange = fNervousRange;
	m_fPrevFovy = m_tCameraDesc.fFovy;
}

void CCamera_Dynamic::Start_ShowGun()
{
	m_bMoveCamera = true;
	m_bStart_ShowGun = true;

	XMStoreFloat3(&m_vPlayerCamPos, m_pTransform->Get_State(CTransform::STATE_POS));
}

void CCamera_Dynamic::Start_PlayerChange(CTransform* pPlayerTransform)
{
	m_bStart_PlayerChange = true;

	XMStoreFloat4x4(&m_PivotMatrix, m_pPivotTransformCom->Get_WorldMatrix());

	m_pPivotTransformCom->Set_State(CTransform::STATE_LOOK, pPlayerTransform->Get_State(CTransform::STATE_LOOK));

	g_Time = 0.2;
}

void CCamera_Dynamic::Start_Section1()
{
	m_bMoveCamera = true;
	XMStoreFloat3(&m_vPrevPos, m_pTransform->Get_State(CTransform::STATE_POS));

	m_pTransform->Set_State(CTransform::STATE_POS, XMVectorSet(41.7f, 8.5f, 46.f, 1.f));
	m_pTransform->LookAt(XMVectorSet(50.f, 0.373f, 46.f, 1.f));
}

void CCamera_Dynamic::Start_ShowBoss2()
{
	m_bStart_ShowBoss2 = true;

	m_pTransform->Set_State(CTransform::STATE_POS, XMVectorSet(38.f, 7.5f, 46.f, 1.f));
}

void CCamera_Dynamic::Start_BackBoss2()
{
	m_bStart_ShowBoss2 = false;
	m_bBack_Camera_Boss2 = true;

	XMStoreFloat3(&m_vCurrentPos, m_pTransform->Get_State(CTransform::STATE_POS));
}

void CCamera_Dynamic::Start_ShowBoss1()
{
	m_bMoveCamera = true;
	m_bStart_ShowBoss1 = true;

	XMStoreFloat3(&m_vPrevPos, m_pTransform->Get_State(CTransform::STATE_POS));

	GAME_INSTANCE;

	CTransform* pBoss1Transform = static_cast<CTransform*>(pGameInstance->Get_Component(LEVEL_STAGE1, TEXT("Layer_Section3"), m_pTransformTag));
	if (nullptr == pBoss1Transform)
		return;

	_vector vLook = pBoss1Transform->Get_State(CTransform::STATE_LOOK);
	_vector vPos = pBoss1Transform->Get_State(CTransform::STATE_POS);

	POS_Y(vPos) += 6.f;

	m_pTransform->Set_State(CTransform::STATE_POS, vPos - XMVector3Normalize(vLook) * 10.f);
	m_pTransform->LookAt(vPos);

	XMStoreFloat3(&m_vMiddlePos, vPos - XMVector3Normalize(vLook) * 30.f);

	GAME_RELEASE;
}

void CCamera_Dynamic::Start_BackBoss1()
{
	m_bStart_ShowBoss1 = false;
	m_bBack_Camera_Boss1 = true;
	m_fBackTime = 0.f;

	XMStoreFloat3(&m_vCurrentPos, m_pTransform->Get_State(CTransform::STATE_POS));
}

_bool CCamera_Dynamic::Limit_Rotation(_fvector vAxis, _float fDegree)
{
	_vector vLook = XMVector3Normalize(m_pPivotTransformCom->Get_State(CTransform::STATE_LOOK));

	_vector vResult = XMVector3Dot(vLook, vAxis);

	_float fRadian = acosf(XMVectorGetX(vResult));

	if (XMConvertToRadians(fDegree) < fRadian)
		return true;
	else
		return false;
}

void CCamera_Dynamic::Camera_Shake(_double TimeDelta)
{
	m_fShakeTime += (_float)TimeDelta;
	if (m_fShakeTime >= m_fFinishShakeTime)
	{
		m_fShakeTime = 0.f;
		m_isShaking = false;

		if (true == m_bHasPos)
			m_pTransform->Set_State(CTransform::STATE_POS, XMLoadFloat4(&_float4(m_vPrevCamPos, 1.f)));

		return;
	}

	_vector vCamPos;

	if (true == m_bHasPos)
		vCamPos = XMLoadFloat4(&_float4(m_vPrevCamPos, 1.f));
	else
		vCamPos = m_pTransform->Get_State(CTransform::STATE_POS);

	uniform_real_distribution<_float> dist(-(m_fShakeRange * 0.5f), m_fShakeRange * 0.5f);
	
	vCamPos = XMVectorSetY(vCamPos, XMVectorGetY(vCamPos) + dist(g_Random));
	vCamPos = XMVectorSetX(vCamPos, XMVectorGetX(vCamPos) + dist(g_Random));

	m_pTransform->Set_State(CTransform::STATE_POS, vCamPos);
}

void CCamera_Dynamic::Camera_Nervous(_double TimeDelta)
{
	m_fNervousTime += (_float)TimeDelta;
	if (m_fNervousTime >= m_fFinishNervousTime)
	{
		m_fNervousTime = 0.f;
		m_isNervous = false;
		m_tCameraDesc.fFovy = m_fPrevFovy;
		return;
	}

	uniform_real_distribution<_float> dist(60.f - m_fNervousRange * 0.5f, 60.f + m_fNervousRange * 0.5f);

	m_tCameraDesc.fFovy = XMConvertToRadians(dist(g_Random));
}

void  CCamera_Dynamic::Show_Gun(_double TimeDelta)
{
	m_fLerpTime += (_float)TimeDelta * 0.1f;
	if (m_fLerpTime >= 1.f)
	{
		m_bMoveCamera = false;
		m_bStart_ShowGun = false;
		m_fLerpTime = 0.f;
		m_pTransform->Set_State(CTransform::STATE_POS, XMLoadFloat4(&_float4(m_vPlayerCamPos, 1.f)));

		CGameInstance* pGameInstance = GET_INSTANCE(CGameInstance);

		for (_uint i = 0; i < 4; ++i)
		{
			((CGun*)pGameInstance->Get_GameObject(LEVEL_STAGE1, TEXT("Layer_Gun"), i))->Set_Start();
		}

		RELEASE_INSTANCE(CGameInstance);

		return;
	}
	
	_vector vLookAtLerpFisrt = XMVectorLerp(XMLoadFloat3(&_float3(67.f, 12.727f, 50.5f)), XMLoadFloat3(&_float3(130.f, 20.9f, 80.f)), m_fLerpTime);
	_vector vLookAtLerpSecond = XMVectorLerp(XMLoadFloat3(&_float3(130.f, 20.9f, 80.f)), XMLoadFloat3(&_float3(105.05f, 18.9f, 12.f)), m_fLerpTime);
	_vector vLookAt = XMVectorLerp(vLookAtLerpFisrt, vLookAtLerpSecond, m_fLerpTime);

	m_pTransform->LookAt(XMVectorSetW(vLookAt, 1.f));
	
	_vector vFirstLerp = XMVectorLerp(XMLoadFloat3(&_float3(74.f, 26.f, 63.8f)), XMLoadFloat3(&_float3(120.1f, 31.4f, 66.8f)), m_fLerpTime);
	_vector vSecondLerp = XMVectorLerp(XMLoadFloat3(&_float3(120.1f, 31.4f, 66.8f)), XMLoadFloat3(&_float3(113.9f, 30.5f, 12.5f)), m_fLerpTime);
	_vector vCamPos = XMVectorLerp(vFirstLerp, vSecondLerp, m_fLerpTime);

	m_pTransform->Set_State(CTransform::STATE_POS, XMVectorSetW(vCamPos, 1.f));
}

void CCamera_Dynamic::Show_PlayerChange(_double TimeDelta)
{
	GAME_INSTANCE;

	CPlayer* pPlayer = static_cast<CPlayer*>(pGameInstance->Get_GameObject(LEVEL_STAGE1, LAYER_PLAYER));
	if (nullptr == pPlayer)
		return;

	GAME_RELEASE;

	_uint iPlayerState = pPlayer->Get_CurrentState();

	if (Berserker::CBA_SPEAR != iPlayerState && Berserker::CBA_SWORD != iPlayerState && Berserker::CB_SPEAR != iPlayerState && Berserker::CB_SWORD!= iPlayerState)
	{
		m_bMoveCamera = false;
		m_bStart_PlayerChange = false;

		m_pPivotTransformCom->Set_WorldFloat4x4(m_PivotMatrix);

		g_Time = 1.0;
		return;
	}

	m_pPivotTransformCom->Turn(XMVectorSet(0.f, 1.f, 0.f, 0.f), TimeDelta * 2.3f);

	_vector vPos = m_pPivotTransformCom->Get_State(CTransform::STATE_POS) + m_pPivotTransformCom->Get_State(CTransform::STATE_LOOK) * 2.5f;

	m_pTransform->Set_State(CTransform::STATE_POS, vPos);
	m_pTransform->LookAt(m_pPivotTransformCom->Get_State(CTransform::STATE_POS));
}

void CCamera_Dynamic::Show_Boss2(_double TimeDelta)
{
	GAME_INSTANCE;

	CTransform* pBoss2Transform = static_cast<CTransform*>(pGameInstance->Get_Component(LEVEL_STAGE1, TEXT("Layer_Boss2"), m_pTransformTag));
	if (nullptr == pBoss2Transform)
		return;

	m_pTransform->LookAt(pBoss2Transform->Get_State(CTransform::STATE_POS));

	GAME_RELEASE;
}

void CCamera_Dynamic::Back_Boss2(_double TimeDelta)
{
	m_fBackTime += (_float)TimeDelta * 0.4f;
	if (1.f <= m_fBackTime)
	{
		m_bBack_Camera_Boss2 = false;
		m_bMoveCamera = false;
		m_fBackTime = 1.f;
	}

	_vector vPos = XMVectorLerp(XMLoadFloat3(&m_vCurrentPos), XMLoadFloat3(&m_vPrevPos), m_fBackTime);

	vPos.m128_f32[3] = 1.f;
	
	m_pTransform->Set_State(CTransform::STATE_POS, vPos);

	GAME_INSTANCE;
	_vector vLookAt = static_cast<CTransform*>(pGameInstance->Get_Component(LEVEL_STAGE1, TEXT("Layer_Boss2"), m_pTransformTag))->Get_State(CTransform::STATE_POS);
	m_pTransform->LookAt(vLookAt);
	GAME_RELEASE;
}

void CCamera_Dynamic::Back_Boss1(_double TimeDelta)
{
	m_fBackTime += (_float)TimeDelta * 0.2f;
	if (1.f <= m_fBackTime)
	{
		m_bBack_Camera_Boss1 = false;
		m_bMoveCamera = false;
		m_fBackTime = 1.f;
	}

	_vector vFirstLerp = XMVectorLerp(XMLoadFloat3(&m_vCurrentPos), XMLoadFloat3(&m_vMiddlePos), m_fBackTime);
	_vector vSecondLerp = XMVectorLerp(XMLoadFloat3(&m_vMiddlePos), XMLoadFloat3(&m_vPrevPos), m_fBackTime);
	_vector vPos = XMVectorLerp(vFirstLerp, vSecondLerp, m_fBackTime);

	vPos.m128_f32[3] = 1.f;

	m_pTransform->Set_State(CTransform::STATE_POS, vPos);

	if (0.7f >= m_fBackTime)
	{
		GAME_INSTANCE;
		_vector vLookAt = static_cast<CTransform*>(pGameInstance->Get_Component(LEVEL_STAGE1, TEXT("Layer_Section3"), m_pTransformTag))->Get_State(CTransform::STATE_POS);
		m_pTransform->LookAt(vLookAt);
		GAME_RELEASE;
	}
	else
	{
		GAME_INSTANCE;
		_vector vLookAt = static_cast<CTransform*>(pGameInstance->Get_Component(LEVEL_STAGE1, LAYER_PLAYER, m_pTransformTag))->Get_State(CTransform::STATE_POS);
		m_pTransform->LookAt(vLookAt);
		GAME_RELEASE;
	}
}

CCamera_Dynamic * CCamera_Dynamic::Create(ID3D11Device* pDevice, ID3D11DeviceContext* pDeviceContext)
{
	CCamera_Dynamic*	pInstance = new CCamera_Dynamic(pDevice, pDeviceContext);

	if (FAILED(pInstance->NativeConstruct_Prototype()))
	{
		MSGBOX("Failed to Creating CCamera_Dynamic");
		Safe_Release(pInstance);
	}

	return pInstance;
}

CGameObject * CCamera_Dynamic::Clone(void * pArg)
{
	CCamera_Dynamic*	pInstance = new CCamera_Dynamic(*this);

	if (FAILED(pInstance->NativeConstruct(pArg)))
	{
		MSGBOX("Failed to Copying CCamera_Dynamic");
		Safe_Release(pInstance);
	}

	return pInstance;
}

void CCamera_Dynamic::Free()
{
	__super::Free();

	Safe_Release(m_pPivotTransformCom);
	Safe_Release(m_pRotTransformCom);
}

#ifdef _DEBUG
void CCamera_Dynamic::ImGui_Camera(_double TimeDelta)
{
	ImGui::Begin("Camera");
	_float3 vPos;
	XMStoreFloat3(&vPos, m_pTransform->Get_State(CTransform::STATE_POS));
	ImGui::DragFloat3(" Camera Pos", (_float*)&vPos, 0.1f);
	m_pTransform->Set_State(CTransform::STATE_POS, XMLoadFloat4(&_float4(vPos, 1.f)));

	ImGui::End();
}
#endif
